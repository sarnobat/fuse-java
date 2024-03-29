import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

// https://github.com/EtiennePerot/fuse-jna/blob/master/src/main/java/net/fusejna/examples/HelloFS.java
public class HelloFS extends FuseFilesystemAdapterFull {

	public static void main(final String... args) throws FuseException, IOException {
		Path tempDirWithPrefix = Files.createTempDirectory("");
		System.out.println("HelloFS.main() " + tempDirWithPrefix);
		ProcessBuilder pb = new ProcessBuilder("open", tempDirWithPrefix.toAbsolutePath().toString());
		Process p = pb.start();

		new HelloFS().log(false).mount(tempDirWithPrefix.toFile());
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
		return 0;
	}

}
