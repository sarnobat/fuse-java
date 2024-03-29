import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    private static final Set<String> topLevelSubdirs = new HashSet<>();
    private static Path pathContentsScript;
    private static Path pathListDirScript;

    public static void main(final String... args) throws FuseException, IOException {
        String scriptListSubdirs;
        if (args.length == 0) {
            // TODO: rename to dir_contents.sh
            scriptListSubdirs = "list_subdirs.sh";
        } else {
            scriptListSubdirs = args[0];
        }
        pathListDirScript = Paths.get(scriptListSubdirs);

        String scriptListFiles;
        if (args.length == 0) {
            // TODO: rename to dir_contents.sh
            scriptListFiles = "list_files.sh";
        } else {
            scriptListFiles = args[1];
        }
        pathListDirScript = Paths.get(scriptListFiles);
        
        String scriptContents;
        if (args.length == 0) {
            // TODO: rename to file_contents.sh
            scriptContents = "contents.sh";
        } else {
            scriptContents = args[2];
        }
        pathContentsScript = Paths.get(scriptContents);

        if (!pathListDirScript.toFile().exists()) {
            System.err.println("[error] FuseShellScriptCallouts.main() - no  list script: "
                    + pathListDirScript.toAbsolutePath().toString());
            System.exit(-1);
        } else if (!pathContentsScript.toFile().exists()) {
            System.err.println("[error] FuseShellScriptCallouts.main() - no content script: "
                    + pathContentsScript.toAbsolutePath().toString());
            System.exit(-1);

        } else {
            new Thread() {

                @Override
                public void run() {
                    String pathDirList = pathListDirScript.toAbsolutePath().toString();
                    topLevelSubdirs.clear();
                    topLevelSubdirs.addAll(getSubdirsInDir(pathDirList, "/"));
                }

            }.start();
        }
        Path tempDirWithPrefix = Files.createTempDirectory("");
        System.err.println("HelloFS.main()\nfind " + tempDirWithPrefix + " -maxdepth 3 ");
        ProcessBuilder pb = new ProcessBuilder("open", tempDirWithPrefix.toAbsolutePath().toString());
        pb.start();

        new FuseShellScriptCallouts().log(false).mount(tempDirWithPrefix.toFile());
    }

    private static Set<String> getSubdirsInDir(String pathDirList, String path) {
        System.err.println("FuseShellScriptCallouts.getFilesInDir() input: " + pathDirList + " " + path);
        Set<String> files3 = new HashSet<>();
        ProcessBuilder pb = new ProcessBuilder("sh", pathDirList, path);
        try {
            Process processListDir = pb.start();
            {
                new Thread() {

                    @Override
                    public void run() {
                        BufferedReader in = new BufferedReader(new InputStreamReader(processListDir.getErrorStream()));
                        String line;
                        try {
                            while ((line = in.readLine()) != null) {
                                System.err.println(
                                        "FuseShellScriptCallouts.getFilesInDir() stderr of script " + pathDirList + ": " + line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(processListDir.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                files3.add(line);
            }
            try {
                processListDir.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }

            in.close();
            System.err.println("FuseShellScriptCallouts.getFilesInDir() returned: " + files3);
            return files3;

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    @Deprecated
    private final String filename = "/hello.txt";
    @Deprecated
    private final String contents = "Hello World\n";

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

    /**
     * File contents
     */
    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset,
            final FileInfoWrapper info) {
//        System.out.println("FuseShellScriptCallouts.read() path = " + path);
        final String s = getContentsOf(path);
        buffer.put(s.getBytes());
        return s.getBytes().length;
    }

    private static String getContentsOf(final String path) {
        try {
            Process processGetContents = new ProcessBuilder()
                    .command(pathContentsScript.toAbsolutePath().toString(), path).start();
            InputStream is = processGetContents.getInputStream();
            return new String(is.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return "IMPOSSIBLE";
        }
    }

    /**
     * Directory contents
     */
    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        System.err.println("FuseShellScriptCallouts.readdir() 1 >>" + path +"<<");
//         if (path.equals("/")) {
//             System.err.println("FuseShellScriptCallouts.readdir() 2 topLevelSubdirs = " + topLevelSubdirs.size());
//             filler.add("by_name");
//         } else {
            System.err.println("FuseShellScriptCallouts.readdir() 3 execute script with arg " + path);

            {
                Set<String> subdirsInDir = getSubdirsInDir(pathListDirScript.toAbsolutePath().toString(), path);
//                System.err.println("FuseShellScriptCallouts.readdir() " + subdirsInDir.size());
               filler.add(subdirsInDir);
            }
            {
//                 filler.add(Paths.get(path).getFileName().toString());
            }
//         }
        return 0;
    }

}
