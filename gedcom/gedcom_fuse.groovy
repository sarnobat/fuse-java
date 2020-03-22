import java.io.File;
import java.nio.ByteBuffer;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

public class App extends FuseFilesystemAdapterFull {
    
    public static void main(String... args) throws FuseException {
        // Strange - groovy ignores arg1's hardcoding. Maybe it's not an acceptable
        // array initialization in groovy?
//        final String[] args1 = args;// { "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
        if (args.length != 1) {
            System.err.println("Usage: HelloFS <mountpoint>");
            System.exit(1);
//          args = new String[]{ "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
        }
        new App().log(true).mount(args[0]);
    }

    private static final String filename = "/hello1.txt";
    private static final String contents = "Hello World\n";

    @Override
    public int getattr(final String path, final StatWrapper stat) {
        stat.setAllTimesMillis(System.currentTimeMillis());
        if (path.equals(File.separator)) { // Root directory
            stat.setMode(NodeType.DIRECTORY);
            return 0;
        }
        if (path.equals(filename)) { // hello.txt
            stat.setMode(NodeType.FILE).size(contents.length());
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset,
            final FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        final String s = contents.substring((int) offset,
                (int) Math.max(offset, Math.min(contents.length() - offset, offset + size)));
        buffer.put(s.getBytes());
        System.out.println("SRIDHAR App.read() " + s);
        return s.getBytes().length;
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        filler.add(filename);
        filler.add("sridhar.txt");
        return 0;
    }
}
