import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import net.fusejna.DirectoryFiller;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

// https://github.com/EtiennePerot/fuse-jna/blob/master/src/main/java/net/fusejna/examples/HelloFS.java
public class FuseShellScriptCallouts extends FuseFilesystemAdapterFull {

    private static final Set<String> files = new HashSet<>();

    public static void main(final String... args) throws FuseException, IOException {
        String scriptList;
        if (args.length == 0) {
            scriptList = "list.sh";
        } else {
            scriptList = args[0];
        }
        Path p = Paths.get(scriptList);
        if (!p.toFile().exists()) {
            System.out.println("[error] FuseShellScriptCallouts.main() - no file: " + p.toAbsolutePath().toString());
            System.exit(-1);
        } else {
            new Thread() {

                @Override
                public void run() {
                    ProcessBuilder pb = new ProcessBuilder("sh", p.toAbsolutePath().toString());
                    try {
                        Process p = pb.start();
                        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line;
                        while ((line = in.readLine()) != null) {
                            files.add(line);
                        }
                        try {
                            p.waitFor();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            System.exit(-1);
                        }

                        in.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }

            }.start();
        }
        Path tempDirWithPrefix = Files.createTempDirectory("");
        System.out.println("HelloFS.main() " + tempDirWithPrefix);
        ProcessBuilder pb = new ProcessBuilder("open", tempDirWithPrefix.toAbsolutePath().toString());
        pb.start();

        new FuseShellScriptCallouts().log(false).mount(tempDirWithPrefix.toFile());
    }

    final String filename = "/hello.txt";
    final String contents = "Hello World\n";

    @Override
    public int getattr(final String path, final StatWrapper stat) {
        if (path.equals(File.separator)) { // Root directory
            stat.setMode(NodeType.DIRECTORY);
            return 0;
        }
        if (path.equals(filename)) { // hello.txt
            stat.setMode(NodeType.FILE).size(contents.length());
            return 0;
        }
        if (path.endsWith(".txt")) {
            stat.setMode(NodeType.FILE).size(getContentsOf(path).length());
            return 0;
        } else {
            stat.setMode(NodeType.DIRECTORY);
            return 0;
        }

    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset,
            final FileInfoWrapper info) {
        System.out.println("FuseShellScriptCallouts.read() path = " + path);
        final String s = getContentsOf(path);
//        .substring((int) offset,
//                (int) Math.max(offset, Math.min(contents.length() - offset, offset + size)));
        buffer.put(s.getBytes());
        return s.getBytes().length;
    }

    private static String getContentsOf(final String path) {
        String string = "Contents of " + path;
        return string;
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        if (path.equals("/")) {
            filler.add(files);
        } else {
            filler.add(Paths.get(path).getFileName().toString() + ".txt");
        }
        return 0;
    }

}
