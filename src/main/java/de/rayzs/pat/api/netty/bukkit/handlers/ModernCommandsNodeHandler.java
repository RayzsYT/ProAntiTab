package de.rayzs.pat.api.netty.bukkit.handlers;

import de.rayzs.pat.api.netty.bukkit.BukkitPacketHandler;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class ModernCommandsNodeHandler implements BukkitPacketHandler {

    /**
     *
     * Note for me:
     *
     *
     * This is just the current process to block sub-arguments from commands created with the
     * Mojang Brigadier Library.
     *
     * The code for the proxy (Velocity / Bungeecord) is already working flawlessly.
     *
     * This class is solely for Bukkit server.
     *
     * Currently, the packet only gets fragmented and rebuild in a more visually operative
     * design to work with.
     *
     * Currently, there is no filtering process.
     *
     * I think imma going to reconstruct the packet and send it back to the player.
     * But I would prefer editing the packet instead and just let it be.
     *
     * But the children are in a fixed sized.
     *
     * I am thinking of changing the index of the child in question to the index of a
     * node return nothing back. This way, I would not have to edit much and could just
     * stop the tab-completion this way.
     */
    public boolean handleIncomingPacket(Player player, CommandSender sender, Object packetObj) throws Exception {
        // Ignored
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(Player player, CommandSender sender, Object packetObj) throws Exception {
        final FilterProcess process = new FilterProcess(packetObj);

        removeSubargument(process, "f admin");
        removeSubargument(process, "f ahome");

        return true;
    }

    public void removeSubargument(FilterProcess process, String command) throws Exception {
        final String[] args = command.split(" ");
        final FilterProcess.Node child = process.getRoot().getChild(args[0]);

        removeSubargument(child, 0, args);
    }

    public void removeSubargument(FilterProcess.Node parent, int index, String[] args) throws Exception {
        final int max = args.length - 1;
        final int nextIndex = index + 1;

        System.out.println("Current next: " + nextIndex);

        if (index > max || nextIndex > max) {
            return;
        }

        final String nextPart = args[nextIndex];
        System.out.println("Which is: " + nextPart);

        if (nextIndex == max) {
            System.out.println("Yes, imma gonna remove it");
            parent.removeChild(nextPart);
            return;
        }

        System.out.println("Going to next part actually");

        final FilterProcess.Node nextParent = parent.getChild(nextPart);
        if (nextParent == null) {
            System.out.println("Nothing found");
            return;
        }

        System.out.println("Recursive");
        removeSubargument(nextParent, nextIndex + 1, args);
    }

    public class FilterProcess {

        private final List<SimpleNode> nodeList = new ArrayList<>();
        private final Object packetObj;
        private final List entries;
        private final Node root;

        public FilterProcess(Object packetObj) throws Exception {
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

        private Node createRootNode(FilterProcess process, int rootIndex, final SimpleNode[] nodes) {
            return new Node(process, nodes[rootIndex], nodes);
        }

        private static class Node {

            private final int rootIndex, index;
            private final List<Node> children;
            private final SimpleNode simpleNode;
            private final FilterProcess process;

            @Nullable
            private final String name;

            public Node(FilterProcess process, SimpleNode node, final SimpleNode[] nodes) {
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

            public void removeChild(String childName) throws Exception {
                final Node child = getChild(childName);

                if (child != null) {
                    System.out.println("Found and remove child: " + childName);
                    this.children.remove(child);
                    this.process.entries.set(child.index, this.process.createEmptyEntry());
                } else System.out.println("No such child: " + childName);
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
}
