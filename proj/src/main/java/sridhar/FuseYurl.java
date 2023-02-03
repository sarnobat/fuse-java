package sridhar;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

/**
 * 2018-02
 */
public class FuseYurl extends FuseFilesystemAdapterFull {

    @Deprecated
    private static final String URLS = System.getProperty("user.home") + "/db.git/yurl_flatfile_db/yurl_master.txt";
    @Deprecated
    private static final String HIERARCHY = System.getProperty("user.home")
            + "/db.git/yurl_flatfile_db/yurl_category_topology.txt";
    @Deprecated
    private static final String CATEGORIES = System.getProperty("user.home")
            + "/db.git/yurl_flatfile_db/yurl_categories_master.txt";
    @Deprecated
    private static final Map<String, String> dirName2dirId = new HashMap<String, String>();
    @Deprecated
    private static final Map<String, String> dirId2dirName = new HashMap<String, String>();
    @Deprecated
    private static final Map<String, String> dirId2ParentId = new HashMap<String, String>();
    @Deprecated
    private static final Multimap<String, String> dirId2ChildDirIds = ArrayListMultimap.create();
    @Deprecated
    private static final Multimap<String, String> dirId2Urls = ArrayListMultimap.create();
    private static final Map<String, Map<String, String>> dirId2filenames = new HashMap<>();
    // @Deprecated
    // private static final String string = System.getProperty("user.home") +
    // "/github/fuse-java/yurl";

    public static void main(String... args) throws FuseException, IOException {
        // Strange - groovy ignores arg1's hardcoding. Maybe it's not an
        // acceptable array initialization in groovy?
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
                        e.printStackTrace();
                    } catch (FuseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        new Thread() {

            @Override
            public void run() {

                try {
                    List<String> l = FileUtils.readLines(Paths.get(URLS).toFile(), "UTF-8");
                    for (String s : l) {
                        String[] elems = s.split("::");
                        if (elems.length == 3) {
                            System.out.println("FuseYurl.main() elems[0] = " + elems[0]);
                            System.out.println("FuseYurl.main() elems[1] = " + elems[1]);
                            System.out.println("FuseYurl.main() elems[2] = " + elems[2]);
                            dirId2Urls.put(elems[0], elems[1]);
                            if (!dirId2filenames.containsKey(elems[0])) {
                                dirId2filenames.put(elems[0], new HashMap<>());
                            }
                            dirId2filenames.get(elems[0]).put(elems[1], url2filename(elems[1]));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Thread() {

            @Override
            public void run() {
                try {
                    List<String> l = FileUtils.readLines(Paths.get(CATEGORIES).toFile(), "UTF-8");
                    for (Iterator<String> iterator = l.iterator(); iterator.hasNext();) {
                        String string1 = (String) iterator.next();
                        // System.out.println("FuseYurl.main() string1 = " + string1);
                        String[] elems = string1.split("::");
                        dirName2dirId.put(elems[1], elems[0]);
                        dirId2dirName.put(elems[0], elems[1]);

                    }
                    List<String> l2 = FileUtils.readLines(Paths.get(HIERARCHY).toFile(), "UTF-8");
                    for (Iterator<String> iterator = l2.iterator(); iterator.hasNext();) {
                        String string2 = (String) iterator.next();
                        String[] elems = string2.split("::");
                        dirId2ParentId.put(elems[1], elems[0]);
                        dirId2ChildDirIds.put(elems[0], elems[1]);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("main() ERROR - " + e);
                }

            }

        }.start();
        System.out.println("main() mounting...");
        Path tempDirWithPrefix = Files.createTempDirectory("");
        System.out.println("HelloFS.main() " + tempDirWithPrefix);
        ProcessBuilder pb = new ProcessBuilder("open", tempDirWithPrefix.toAbsolutePath().toString());
        pb.start();
        new FuseYurl().log(false).mount(tempDirWithPrefix.toAbsolutePath().toString());
    }

    // final String filename = "/hello1.txt";
    final String contents = "Hello World\n";

    @Override
    public int readdir(final String path, final DirectoryFiller filler) {
        System.out.println("FuseYurl.readdir() path = " + path);
        if ("/".equals(path)) {
            filler.add("products and services");
            return 0;
        }
        System.out.println("FuseYurl.readdir() size = " + dirName2dirId.size());
        String currentDirName = Paths.get(path).getFileName().toString();
        System.out.println("FuseYurl.readdir() currentDirName = " + currentDirName);
        String currentDirId = dirName2dirId.get(currentDirName);
        System.out.println("FuseYurl.readdir() currentDirId = " + currentDirId);
        if (path.equals(File.separator)) {
            currentDirId = "45";
        }
        Collection<String> s = FluentIterable.from(dirId2ChildDirIds.get(currentDirId))
                .transform(new Function<Object, String>() {

//                        @Override
                    public String apply(Object input) {
                        return dirId2dirName.get(input);
                    }
                }).toList();
        Collection<String> s2 = FluentIterable.from(dirId2Urls.get(currentDirId))
                .transform(new Function<String, String>() {

                    // @Override
                    public String apply(String input) {
                        return url2filename(input);
                    }
                }).toList();

//        Map<String, String> filename2url = dirId2filenames.get(currentDirId);

//		url2filename.forEach((key, value) -> filename2url.put(value, key));

        for (String ss : Iterables.concat(s, s2)) {
            if (!filler.add(ss)) {
                return ErrorCodes.ENOMEM();
            }
        }
        return 0;
    }

    private static String url2filename(String input) {
        String string = ((String) input).replace("http://", "").replace("https://", "").replace("www.", "")
                .replace("-", " ").replace("_", " ").replaceFirst("/", " - ").replace("/", "_") + ".url";
        return string;
    }

    @Override
    public int getattr(final String path, final StatWrapper stat) {
        if (path.equals(File.separator)) { // Root directory
            stat.setMode(NodeType.DIRECTORY);
            return 0;
        } else if (path.endsWith(".url")) {
            System.out.println("FuseYurl.getattr() TODO: get size of file for path = " + path);
            // NOTE: du examines actual disk usage, not the size attribute:
            // https://github.com/restic/restic/issues/442
            stat.setMode(NodeType.FILE).size(contents.length()).blksize(1);
            return 0;

        } else {
            // stat.setMode(NodeType.FILE).size(contents.length());

            String dirName = dirName2dirId.get(path);
            int count = dirId2Urls.get(dirName).size();
            stat.setMode(NodeType.DIRECTORY).size(count).blksize(1000000);
            return 0;
        }
        // return -ErrorCodes.ENOENT();
    }

    @Override
    public int read(final String path, final ByteBuffer buffer, final long size, final long offset,
            final FileInfoWrapper info) {
        System.out.println("FuseYurl.read() path = " + path);
        System.out.println("FuseYurl.read() size = " + size);
        System.out.println("FuseYurl.read() offset = " + offset);
        long min = Math.min(contents.length() - offset, offset + size);
        System.out.println("FuseYurl.read() min = " + min);
        // Compute substring that we are being asked to read
        final String s = contents.substring((int) offset, (int) Math.max(offset, min));
        // final String s = "TODO: get some useful info";
        buffer.put(s.getBytes());
        return s.getBytes().length;
    }
}
