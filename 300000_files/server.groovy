import java.io.File;
import java.nio.ByteBuffer;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

public class HelloWorldFuse extends FuseFilesystemAdapterFull
{
	public static void main(final String... args) throws FuseException
	{
		// Strange - groovy ignores arg1's hardcoding. Maybe it's not an acceptable array initialization in groovy?
		final String[] args1 = args;// { "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
		if (args.length == 1) {
			new HelloWorldFuse().log(false).mount(args[0]);
		}
		else {
			// System.err.println("Usage: HelloFS <mountpoint>");
			// System.exit(1);
			// args = new String[]{ "/sarnobat.garagebandbroken/trash/fuse-jna/mnt" };
			new HelloWorldFuse().log(false).mount("/sarnobat.garagebandbroken/Desktop/github-repositories/fuse-java/mnt");
		}
	}

	final String filename = "/hello1.txt";
	final String contents = "Hello World\n";

	@Override
	public int getattr(final String path, final StatWrapper stat)
	{
		if (path.equals(File.separator)) { // Root directory
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}
		if (path.equals(filename)) { // hello.txt
			stat.setMode(NodeType.FILE).size(contents.length());
			return 0;
		}
		if (path.endsWith("dir")) {
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}
		else if (path.endsWith("txt")) {
			stat.setMode(NodeType.FILE).size(contents.length());
			return 0;
		}
		// A catch-all else causes an infinite recursion
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info)
	{
		// Compute substring that we are being asked to read
		final String s = contents.substring((int) offset,
				(int) Math.max(offset, Math.min(contents.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		return s.getBytes().length;
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler)
	{
		filler.add(filename);
		if (path.length() == 1) {
			filler.add("1dir");
			filler.add("2dir");
			filler.add("3dir");
			filler.add("sridhar.txt");
		}
		if (path.endsWith("1dir")) {
			for (int i = 0; i < 300000; i++) {
				final boolean added = filler.add(i
						+ "-11111-11111-11111-11111-11111-11111-11111-11111-11111-11111-11111-11111-11111-sridhar.txt");
				if (!added) {
					System.out.println("HelloWorldFuse.readdir() Failed to add");
				}
			}
		}
		return 0;
	}
}
