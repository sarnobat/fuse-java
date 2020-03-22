package sridhar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

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
        final String[] args1 = args;// { "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
        if (args.length != 1) {
            System.err.println("Usage: HelloFS <mountpoint>");
            System.exit(1);
//          args = new String[]{ "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
        }
        java.lang.System.setProperty("https.protocols", "TLSv1.2");
        App app = new App();
        app.log(false).mount(args[0]);
        if (app.isMounted()) {
            app.unmount();
        }
    }

    final String filename = "/hello1.txt";
    final String contents = "Hello World!\n";

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
        return -ErrorCodes.ENOENT();
    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset,
            final FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        final String s = contents.substring((int) offset,
                (int) Math.max(offset, Math.min(contents.length() - offset, offset + size)));
        buffer.put(s.getBytes());
        return s.getBytes().length;
    }

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        filler.add(filename);
        filler.add("sridhar.txt");
        
        URL url;
        try {
            url = new URL("https://mailer.cloudapps.cisco.com/itsm/mailer/searchList.do?searchType=listname&searchQuery=*&displayMode=browse&sort=groupName&currentPage=1451");
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return 1;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            for (String line; (line = reader.readLine()) != null;) {
                System.out.println(line);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }
}
