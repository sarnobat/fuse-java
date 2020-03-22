import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fusejna.DirectoryFiller;
import net.fusejna.FuseException;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

public class App extends FuseFilesystemAdapterFull {

    private static Map<Individual, Individual> childToMother = new HashMap<>();
    private static Map<Individual, Individual> childToFather = new HashMap<>();
    private static Map<Individual, String> individualToChildFamilyId = new HashMap<>();
    private static Map<String, String> displayNameOfChildToParent= new HashMap<>();
    private static Map<String, Individual> idToIndividual = new HashMap<>();
    private static Map<String, Family> idToFamily = new HashMap<>();
    private static Set<Individual> individualsWithNoParent = new HashSet<>();

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

            new Thread() {

                @Override
                public void run() {
                    File myObj = new File("/Users/srsarnob/sarnobat.git/gedcom/rohidekar.ged");
                    Scanner myReader;

                    try {
                        myReader = new Scanner(myObj);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    Individual individual = null;
                    Family family = null;
                    while (myReader.hasNextLine()) {
                        String data = myReader.nextLine();
                        if (data.startsWith("0") && data.endsWith("INDI")) {

                            if (individual != null) {
//                                System.out.println(individual.toString());
                            }
                            String regex = "0..(.*)..INDI";
                            Pattern p = Pattern.compile(regex);
                            Matcher matcher = p.matcher(data);
                            if (matcher.find()) {
                                String s = matcher.group(1);
                                individual = new Individual(s);
                                idToIndividual.put(s, individual);
                            } else {
                                throw new RuntimeException("Developer error");
                            }
                            continue;
                        }
                        if (individual == null) {
                            continue;
                        }
                        if (data.startsWith("2 GIVN")) {
                            String replaceAll = data.replaceAll(".*GIVN ", "");
                            individual.setFirstName(replaceAll);
                        } else if (data.startsWith("2 SURN")) {
                            String replaceAll = data.replaceAll(".*SURN ", "");
                            individual.setLastName(replaceAll);
                        } else if (data.startsWith("0") && data.endsWith("FAM")) {
                            String regex = "0..(.*)..FAM";
                            Pattern p = Pattern.compile(regex);
                            Matcher matcher = p.matcher(data);
                            if (matcher.find()) {
                                String s = matcher.group(1);
                                family = new Family(s);
                                idToFamily.put(s, family);
                            } else {
                                throw new RuntimeException("Developer error");
                            }
                        } else if (data.startsWith("1 FAMS")) {
                            String replaceAll = data.replaceAll("1 FAMS .", "").replaceAll(".$", "");
                            individualToChildFamilyId.put(individual, replaceAll);
                        } else if (data.startsWith("1 HUSB")) {
                            String replaceAll = data.replaceAll(".*HUSB .", "").replaceAll(".$", "");
                            Individual husband = idToIndividual.get(replaceAll);
                            family.setHusband(husband);
                        } else if (data.startsWith("1 WIFE")) {
                            String replaceAll = data.replaceAll(".*WIFE .", "").replaceAll(".$", "");
                            family.setWife(idToIndividual.get(replaceAll));
                        } else if (data.startsWith("1 CHIL")) {
                            String replaceAll = data.replaceAll(".*CHIL .", "").replaceAll(".$", "");
                            Individual i = idToIndividual.get(replaceAll);
                            family.addChild(i);
                            i.setParentFamily(family);
                        }
                    }
                    myReader.close();

                    // attach each individual to its family
                    for (Individual i : individualToChildFamilyId.keySet()) {
                        Family f = idToFamily.get(individualToChildFamilyId.get(i));
                        i.setChildFamily(f);
//                        System.out.println("Has parent: " + i.toString());
                    }
                    for (Family f : idToFamily.values()) {
                        for (Individual child : f.getChildren()) {
                            childToFather.put(child, f.getHusband());
                            childToMother.put(child, f.getWife());
                        }
                        f.getHusband().setSpouse(f.getWife());
                        f.getWife().setSpouse(f.getHusband());
                    }

                    // I24 - root
                    System.out.println(printFamily(idToIndividual.get("I24").getChildFamily(), ""));
                }

            }.run();
            ;
            new App().log(true).mount(string);
        }
    }

    private static String printFamily(Family f, String string) {
        if (f == null) {
            return "";
        }
        String s = "";// " " + f.getWife().toString();
        for (Individual c : f.getChildren()) {
            if (c == f.getHusband()) {
                throw new RuntimeException("infinite loop");
            }
            s += "\n" + string + c.toString() + (c.getSpouse() == null ? "" : " -- " + c.getSpouse().toString());
            s += printFamily(c.getChildFamily(), string + "  ");
        }
        return s;
    }

    // TODO: rename this to Marriage
    private static class Family {
        private final String id;
        private Individual husband;
        private Individual wife;
        private final Set<Individual> children = new HashSet<>();

        Individual getHusband() {
            return husband;
        }

        public Iterable<Individual> getChildren() {
            return children;
        }

        public String getId() {
            return id;
        }

        public void addChild(Individual i) {
            children.add(i);
        }

        void setHusband(Individual husband) {
            this.husband = husband;
        }

        Individual getWife() {
            return wife;
        }

        void setWife(Individual wife) {
            this.wife = wife;
        }

        Family(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            String string = id + "  " + husband.toString() + " -- " + wife.toString();
            if (children.size() > 0) {
                string += " (";
                for (Individual i : children) {
                    string += "," + i.toString();
                }
            }
            string += ")";
            return string;
        }
    }

    private static class Individual {
        private final String id;

        String firstName;
        Family childFamily;
        Family parentFamily;
        Individual spouse;

        Family getChildFamily() {
            return childFamily;
        }

        public void setSpouse(Individual husband) {
            this.spouse = husband;
        }

        public Individual getSpouse() {
            return this.spouse;
        }

        void setChildFamily(Family childFamily) {
            this.childFamily = childFamily;
        }

        Family getParentFamily() {
            return parentFamily;
        }

        void setParentFamily(Family parentFamily) {
            this.parentFamily = parentFamily;
        }

        Individual(String id) {
            this.id = id;
        }

        String getFirstName() {
            return firstName;
        }

        void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        String getLastName() {
            return lastName;
        }

        void setLastName(String lastName) {
            this.lastName = lastName;
        }

        String lastName;

        @Override
        public String toString() {
            return firstName + " " + lastName + " " + id;
        }
    }

    private static final String FILENAME = "/hello1.txt";
    private static final String CONTENTS = "Hello World\n";

    @Override
    public int getattr(String path, StatWrapper stat) {
        stat.setAllTimesMillis(System.currentTimeMillis());
        if (path.equals(File.separator)) { // Root directory
            stat.setMode(NodeType.DIRECTORY);
            return 0;
        }
        if (path.equals(FILENAME)) { // hello.txt
            stat.setMode(NodeType.FILE).size(CONTENTS.length());
            return 0;
        } else {
            stat.setMode(NodeType.FILE).size(CONTENTS.length());
            return 0;
        }
        // return -ErrorCodes.ENOENT();
    }

    @Override
    public int read(String path, ByteBuffer buffer, long size, long offset, final FileInfoWrapper info) {
        // Compute substring that we are being asked to read
        final String fileContents = CONTENTS.substring((int) offset,
                (int) Math.max(offset, Math.min(CONTENTS.length() - offset, offset + size)));
        buffer.put(fileContents.getBytes());
        System.out.println("SRIDHAR App.read() " + fileContents);
        return fileContents.getBytes().length;
    }

    @Override
    public int readdir(String path, DirectoryFiller filler) {
        filler.add(FILENAME);
        filler.add("sridhar.txt");
        System.out.println("SRIDHAR App.readdir() " + path);
        for (Entry<String, String> childToParent : displayNameOfChildToParent.entrySet()) {
            String parent = childToParent.getValue();
            if (parent.equals(path)) {
                filler.add(childToParent.getKey());
            }
        }
        String key = "I31";
        Individual individual = idToIndividual.get(key);
        String string = individual.toString();
        filler.add(string);
        for (Individual c : individual.getChildFamily().getChildren()) {
            displayNameOfChildToParent.put(c.toString(), string);
        }
        return 0;
    }

    @Override
    public int rename(String oldName, String newName) {
        System.out.println("SRIDHAR App.rename() mv " + oldName + " " + newName);
        return 0;

    }
}
