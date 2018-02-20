import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;

/**
 * 2018-02
 */
public class FuseYurl extends FuseFilesystemAdapterFull {

	private static final String URLS = System.getProperty("user.home")			+ "/sarnobat.git/db/yurl_flatfile_db/yurl_master.txt";
	private static final String HIERARCHY = System.getProperty("user.home")			+ "/sarnobat.git/db/yurl_flatfile_db/yurl_category_topology.txt";
	private static final String CATEGORIES = System.getProperty("user.home")			+ "/sarnobat.git/db/yurl_flatfile_db/yurl_categories_master.txt";
	private static final Map<String, String> dirName2dirId = new HashMap<String, String>();
	private static final Map<String, String> dirId2dirName = new HashMap<String, String>();
	private static final Map<String, String> dirId2ParentId = new HashMap<String, String>();
	private static final Multimap<String, String> dirId2ChildDirIds = ArrayListMultimap
			.create();

	public static void main(String... args) throws FuseException, IOException {
		// Strange - groovy ignores arg1's hardcoding. Maybe it's not an
		// acceptable array initialization in groovy?
		String string = System.getProperty("user.home") +  "/github/fuse-java/yurl";
		if (new FuseYurl().log(false).isMounted()) {
			new FuseYurl().log(false).unmount();
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				if (new FuseYurl().log(false).isMounted()) {
					try {
						new FuseYurl().log(false).unmount();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FuseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		new Thread() {

			@Override
			public void run() {
				try {
					List<String> l = FileUtils.readLines(Paths.get(CATEGORIES)
							.toFile(), "UTF-8");
					System.out.println("FuseYurl.main() " + l);
					for (Iterator iterator = l.iterator(); iterator.hasNext();) {
						String string1 = (String) iterator.next();
						String[] elems = string1.split("::");
						dirName2dirId.put(elems[1], elems[0]);
						dirId2dirName.put(elems[0], elems[1]);

					}

					List<String> l2 = FileUtils.readLines(Paths.get(HIERARCHY)
							.toFile(), "UTF-8");
					for (Iterator iterator = l2.iterator(); iterator.hasNext();) {
						String string2 = (String) iterator.next();
						System.out.println("FuseYurl.main() - " + string);
						String[] elems = string2.split("::");
						dirId2ParentId.put(elems[1], elems[0]);
						dirId2ChildDirIds.put(elems[0], elems[1]);
					}
					System.out.println("FuseYurl.readdir() " + l);
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("FuseYurl.main() ERROR - " + e);
				}

			}

		}.start();
		System.out.println("FuseYurl.main() mounting...");
		new FuseYurl().log(false).mount(string);
	}

	final String filename = "/hello1.txt";
	final String contents = "Hello World!\n";

	@Override
	public int readdir(final String path, final DirectoryFiller filler) {
		System.out.println("readdir() - path = " + path);
		if (path.equals(File.separator)) {

			Collection<String> s = FluentIterable
					.from(dirId2ChildDirIds.get("45"))
					.transform(new Function() {

						@Override
						public String apply(Object input) {
							return dirId2dirName.get(input);
						}
					}).toList();
			filler.add(s);
		} else {
			System.out.println("FuseYurl.readdir() 2 " + path);
			String currentDirName = Paths.get(path).getFileName().toString();
			String currentDirId = dirName2dirId.get(currentDirName);
			Collection<String> s = FluentIterable
					.from(dirId2ChildDirIds.get(currentDirId))
					.transform(new Function() {

						@Override
						public String apply(Object input) {
							return dirId2dirName.get(input);
						}
					}).toList();

			for (String ss : s) {
				if (!filler.add(ss)) {
					return ErrorCodes.ENOMEM();
				}
			}

		}
		// filler.add("sridhar.txt");
		// filler.add(filename);
		return 0;
	}

	@Override
	public int getattr(final String path, final StatWrapper stat) {
	System.out.println("getattr() path = " + path);
		if (path.equals(File.separator)) { // Root directory
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		} else {
			//stat.setMode(NodeType.FILE).size(contents.length());
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}
		// return -ErrorCodes.ENOENT();
	}

	@Override
	public int read(final String path, final ByteBuffer buffer,
			final long size, final long offset, final FileInfoWrapper info) {
		// Compute substring that we are being asked to read
		final String s = contents.substring(
				(int) offset,
				(int) Math.max(offset,
						Math.min(contents.length() - offset, offset + size)));
		buffer.put(s.getBytes());
		return s.getBytes().length;
	}
}
