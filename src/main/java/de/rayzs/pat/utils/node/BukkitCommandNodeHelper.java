package de.rayzs.pat.utils.node;

import de.rayzs.pat.utils.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class BukkitCommandNodeHelper {

    private final List<SimpleNode> nodeList = new ArrayList<>();
    private final Object packetObj;
    private final List entries;
    private final Node root;

    public BukkitCommandNodeHelper(Object packetObj) throws Exception {
        this.packetObj = packetObj;

        final Class<?> commandsPacketClazz = packetObj.getClass();
        final Field rootIndexField = commandsPacketClazz.getDeclaredField("rootIndex");
        rootIndexField.setAccessible(true);

        final int rootIndex = rootIndexField.getInt(packetObj);
        rootIndexField.setAccessible(false);


        final Field entriesField = commandsPacketClazz.getDeclaredField("entries");
        entriesField.setAccessible(true);

        entries = (List) entriesField.get(packetObj);
        for (int i = 0; i < entries.size(); i++) {
            final Object nodeObj = entries.get(i);
            final Class<?> entryClass = nodeObj.getClass();
            final Field nodeChildrenField = entryClass.getDeclaredField("children");
            nodeChildrenField.setAccessible(true);


            int[] children = (int[]) nodeChildrenField.get(nodeObj);
            nodeChildrenField.setAccessible(false);


            final Field stubField = entryClass.getDeclaredField("stub");
            stubField.setAccessible(true);

            String name = null;

            final Object stubObj = stubField.get(nodeObj);
            if (stubObj != null) {
                final Class<?> stubClass = stubObj.getClass();
                final Field idField = stubClass.getDeclaredField("id");
                idField.setAccessible(true);

                name = idField.get(stubObj).toString();

                idField.setAccessible(false);
            }

            stubField.setAccessible(false);

            final SimpleNode simpleNode = new SimpleNode(rootIndex, i, children, name);
            nodeList.add(simpleNode);
        }

        root = createRootNode(this, rootIndex, nodeList.toArray(new SimpleNode[0]));
    }

    public void removeSubArguments(String command) throws Exception {
        final String[] args = command.split(" ");
        final Node child = getRoot().getChild(args[0]);

        removeSubArguments(child, 0, args);
    }

    public void spareRecursively(List<String> spares) throws Exception {
        spareRecursively("", root, spares);
    }

    private void spareRecursively(String str, Node parent, List<String> spares) throws Exception {
        final List<Node> children = new ArrayList<>(parent.getChildren());

        for (final Node child : children) {

            if (parent.isRoot()) {
                spareRecursively(child.getName(), child, spares);
                continue;
            }

            final String command = str + " " + child.getName();
            final String[] commandArgs = command.split(" ");

            boolean found = false;
            for (String spare : spares) {
                final String[] spareArgs = spare.split(" ");

                if (!spareArgs[0].equals(commandArgs[0])) {
                    continue;
                }

                boolean match = true;
                for (int i = 1; i < spareArgs.length; i++) {
                    final String s = spareArgs[i];

                    if (commandArgs.length - 1 < i) {
                        continue;
                    }

                    if (commandArgs.length - 1 == i && s.equals("_-")) {
                        match = false;
                        break;
                    }

                    if (s.startsWith("%") && s.endsWith("%")) {
                        continue;
                    }

                    if (!s.equals(commandArgs[i])) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                parent.removeChild(child);
            } else {
                spareRecursively(command, child, spares);
            }
        }
    }

    private void removeSubArguments(Node parent, int index, String[] args) throws Exception {
        final int max = args.length - 1;
        final int nextIndex = index + 1;

        if (index > max || nextIndex > max) {
            return;
        }

        final String nextPart = args[nextIndex];

        if (nextIndex == max) {
            parent.removeChild(nextPart);
            return;
        }

        final Node nextParent = parent.getChild(nextPart);
        if (nextParent == null) {
            return;
        }

        removeSubArguments(nextParent, nextIndex + 1, args);
    }

    public Node getRoot() {
        return root;
    }

    private Object createEmptyEntry() throws Exception {
        final String path = packetObj.getClass().getPackage().getName() + "." + packetObj.getClass().getSimpleName();

        final String literalNodeStubPath = path + "$LiteralNodeStub";
        final String entryPath = path + "$Entry";
        final String nodeStubPath = path + "$NodeStub";

        final int[] children = {};

        final Class<?> nodeStubClazz = Class.forName(nodeStubPath);

        final Class<?> literalNodeClazz = Class.forName(literalNodeStubPath);
        final Constructor<?> literalNodeConstructor = literalNodeClazz.getDeclaredConstructor(String.class);
        literalNodeConstructor.setAccessible(true);

        final Object literalNodeObj = literalNodeConstructor.newInstance("");

        final Class<?> entryClazz = Class.forName(entryPath);
        final Constructor<?> entryConstructor = entryClazz.getDeclaredConstructor(nodeStubClazz, int.class, int.class, int[].class);
        entryConstructor.setAccessible(true);

        final Object entryObj = entryConstructor.newInstance(literalNodeObj, 1, 0, children);


        literalNodeConstructor.setAccessible(false);
        entryConstructor.setAccessible(false);


        return entryObj;
    }

    private Node createRootNode(BukkitCommandNodeHelper process, int rootIndex, final SimpleNode[] nodes) {
        return new Node(process, nodes[rootIndex], nodes);
    }

    private static class Node {

        private final int rootIndex, index;
        private final List<Node> children;
        private final SimpleNode simpleNode;
        private final BukkitCommandNodeHelper process;

        @Nullable
        private final String name;

        public Node(BukkitCommandNodeHelper process, SimpleNode node, final SimpleNode[] nodes) {
            this.process = process;
            this.simpleNode = node;

            this.rootIndex = node.rootIndex;
            this.index = node.index;

            this.name = node.name;
            this.children = new ArrayList<>();

            if (!node.isRoot() && name == null) {
                return;
            }

            for (int childIndex : node.children()) {
                final Node child = new Node(process, nodes[childIndex], nodes);
                this.children.add(child);
            }
        }

        public void removeChild(int childIndex) throws Exception {
            final Node child = getChild(childIndex);

            if (child != null) {
                this.children.remove(child);
                this.process.entries.set(child.index, this.process.createEmptyEntry());
            }
        }

        public void removeChild(Node child) throws Exception {
            if (child != null) {
                this.children.remove(child);
                this.process.entries.set(child.index, this.process.createEmptyEntry());
            }
        }

        public void removeChild(String childName) throws Exception {
            final Node child = getChild(childName);

            if (child != null) {
                this.children.remove(child);
                this.process.entries.set(child.index, this.process.createEmptyEntry());
            }
        }

        public SimpleNode getSimpleNode() {
            return simpleNode;
        }

        public int getRootIndex() {
            return rootIndex;
        }

        public int getIndex() {
            return index;
        }

        public boolean isRoot() {
            return index == rootIndex;
        }

        public List<Node> getChildren() {
            return children;
        }

        public Node getChild(int index) {
            return children.get(index);
        }

        public Node getChild(String name) {
            return this.children.stream().filter(n -> n.getName() != null && n.getName().equals(name)).findFirst().orElse(null);
        }

        public @Nullable String getName() {
            return name;
        }
    }


    private record SimpleNode(int rootIndex, int index, int[] children, @Nullable String name) {
        public boolean isRoot() {
            return index == rootIndex;
        }
    }
}