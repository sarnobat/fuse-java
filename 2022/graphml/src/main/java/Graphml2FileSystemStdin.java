
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Graphml2FileSystemStdin {

    public static void main(String[] args) {

        Map<String, Node> nodeDetails = new HashMap<>();
        Multimap<String, String> parentToChildren = HashMultimap.create();
        Set<String> nonRootNodeIds = new HashSet<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = br.readLine()) != null) {
                // log message
                System.err.println("[DEBUG] current line is: " + line);

                String[] elements = line.split("::");
                if ("NODE".equals(elements[0])) {
                    String id = elements[1];
                    String description = elements[2];
                    nodeDetails.put(id, new Node(id, description));
                } else if ("EDGE".equals(elements[0])) {
                    String parent = elements[1];
                    String child = elements[2];
                    nonRootNodeIds.add(child);
                    parentToChildren.put(parent, child);
                } else {

                }

            }
            for (String parent : parentToChildren.keySet()) {
                for (String child : parentToChildren.get(parent)) {
                    nodeDetails.get(parent).addChild(nodeDetails.get(child));
                }
            }

            Set<String> roots = Sets.difference(parentToChildren.keySet(), nonRootNodeIds);
            for (String root : roots) {
                Node node = nodeDetails.get(root);
                addPaths(node, "");
            }
            // program output
            for (String root : roots) {
                Node node = nodeDetails.get(root);
                printPaths(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void printPaths(Node node) {
        System.out.println(node.getPath());
        for (Node child : node.getChildren()) {
            printPaths(child);
        }
    }

    private static void addPaths(Node node, String trail) {
        node.setPath(trail);
        for (Node child : node.getChildren()) {
            addPaths(child, node.getPath() + "/");
        }
    }

    private static class Node {
        private final String id;
        private final String description;
        private String path;
        private final Set<Node> children = new HashSet<>();

        Node(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String trail) {
            this.path = trail + description;
        }

        public Iterable<Node> getChildren() {
            return ImmutableSet.copyOf(children);
        }

        public void addChild(Node node) {
            children.add(node);
        }

        String getId() {
            return id;
        }

        String getDescription() {
            return description;
        }

    }
}