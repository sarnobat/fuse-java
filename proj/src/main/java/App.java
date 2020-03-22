import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

public class App extends FuseFilesystemAdapterFull {

    public static void main(String... args) throws FuseException, IOException {
        // Strange - groovy ignores arg1's hardcoding. Maybe it's not an acceptable
        // array initialization in groovy?
//        final String[] args1 = args;// { "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
        if (args.length == 1) {
            new App().log(true).mount(args[0]);
        } else {
            System.err.println("Usage: HelloFS <mountpoint>");
            String string = "family_tree";
            String string2 = "/Users/srsarnob/github/fuse-java/proj/" + string;
            new ProcessBuilder().command("diskutil", "unmount", string2).inheritIO().start();
            
            try {
                Files.createDirectory(Paths.get(string2));
            } catch (FileAlreadyExistsException e) {

            }
            new App().log(true).mount(string);
//            System.exit(1);
//          args = new String[]{ "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
        }
    }

    private static final String filename = "/hello1.txt";
    private static final String CONTENTS = "Hello World\n";

    @Override
    public int getattr(final String path, final StatWrapper stat) {
        stat.setAllTimesMillis(System.currentTimeMillis());
        if (path.equals(File.separator)) { // Root directory
            stat.setMode(NodeType.DIRECTORY);
            return 0;
        }
        if (path.equals(filename)) { // hello.txt
            stat.setMode(NodeType.FILE).size(CONTENTS.length());
            return 0;
        } else {
            stat.setMode(NodeType.FILE).size(CONTENTS.length());
            return 0;
        }
        //return -ErrorCodes.ENOENT();
    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset,
            final FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        final String fileContents = CONTENTS.substring((int) offset,
                (int) Math.max(offset, Math.min(CONTENTS.length() - offset, offset + size)));
        buffer.put(fileContents.getBytes());
        System.out.println("SRIDHAR App.read() " + fileContents);
        return fileContents.getBytes().length;
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        filler.add(filename);
        filler.add("sridhar.txt");
        return 0;
    }
    
    @Override
    public int rename(String oldName, String newName) {
        System.out.println("SRIDHAR App.rename() mv " + oldName +" "+newName);
        return 0;
        
    }
}
