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
        final List<SimpleNode> nodeList = new ArrayList<>();

        final Class<?> commandsPacketClazz = packetObj.getClass();
        final Field rootIndexField = commandsPacketClazz.getDeclaredField("rootIndex");
        rootIndexField.setAccessible(true);

        final int rootIndex = rootIndexField.getInt(packetObj);

        final Field entriesField = commandsPacketClazz.getDeclaredField("entries");
        entriesField.setAccessible(true);

        final List entries = (List) entriesField.get(packetObj);
        for (int i = 0; i < entries.size(); i++) {
            final Object nodeObj = entries.get(i);

            System.out.println(nodeObj);

            final Class<?> entryClass = nodeObj.getClass();

            final Field nodeChildrenField = entryClass.getDeclaredField("children");
            nodeChildrenField.setAccessible(true);

            int[] children = (int[]) nodeChildrenField.get(nodeObj);

            final Field stubField = entryClass.getDeclaredField("stub");
            stubField.setAccessible(true);

            String name = null;

            final Object stubObj = stubField.get(nodeObj);
            if (stubObj != null) {
                final Class<?> stubClass = stubObj.getClass();
                final Field idField = stubClass.getDeclaredField("id");
                idField.setAccessible(true);

                name = idField.get(stubObj).toString();
            }

            final SimpleNode simpleNode = new SimpleNode(rootIndex, i, children, name);
            nodeList.add(simpleNode);
        }

        Node root = createRootNode(rootIndex, nodeList.toArray(new SimpleNode[0]));

        for (Node child : root.getChildren()) {
            System.out.println(child.getName());

            for (Node childChild : child.getChildren()) {
                if (childChild.getName() == null) continue;

                System.out.println("  " + childChild.getName());
                for (Node childChildChild : childChild.getChildren()) {
                    if (childChildChild.getName() == null) continue;

                    System.out.println("    " + childChildChild.getName());
                }
            }
        }


        Node node = root.getChild("f");

        entries.set(node.getChild("allywarp").getIndex(), createEmptyEntry(commandsPacketClazz, 1, 0));
        entries.set(node.getChild("?").getIndex(), createEmptyEntry(commandsPacketClazz, 1, 0));
        entries.set(node.getChild("help").getIndex(), createEmptyEntry(commandsPacketClazz, 1, 0));

        return true;
    }

    private Object createEmptyEntry(Class<?> parentClazz, int flag, int redirect) throws Exception {
        final String path = parentClazz.getPackage().getName() + "." + parentClazz.getSimpleName();

        final String literalNodeStubPath = path + "$LiteralNodeStub";
        final String entryPath = path + "$Entry";
        final String nodeStubPath = path + "$NodeStub";

        final int[] children = {};

        System.out.println("Path: " + path);
        System.out.println("LiteralNodeStub path: " + literalNodeStubPath);
        System.out.println("Entry path: " + entryPath);

        final Class<?> nodeStubClazz = Class.forName(nodeStubPath);

        final Class<?> literalNodeClazz = Class.forName(path + "$LiteralNodeStub");
        final Constructor<?> literalNodeConstructor = literalNodeClazz.getDeclaredConstructor(String.class);
        literalNodeConstructor.setAccessible(true);

        Object literalNodeObj = literalNodeConstructor.newInstance("");


        final Class<?> entryClazz = Class.forName(entryPath);
        final Constructor<?> entryConstructor = entryClazz.getDeclaredConstructor(nodeStubClazz, int.class, int.class, int[].class);
        entryConstructor.setAccessible(true);

        return entryConstructor.newInstance(literalNodeObj, flag, redirect, children);
    }

    public Node createRootNode(int rootIndex, final SimpleNode[] nodes) {
        return new Node(nodes[rootIndex], nodes);
    }

    private static class Node {

        private final int rootIndex, index;
        private final List<Node> children;
        private final SimpleNode simpleNode;

        @Nullable
        private final String name;

        public Node(SimpleNode node, final SimpleNode[] nodes) {
            this.simpleNode = node;

            this.rootIndex = node.rootIndex;
            this.index = node.index;

            this.name = node.name;
            this.children = new ArrayList<>();

            if (!node.isRoot() && name == null) {
                return;
            }

            for (int childIndex : node.children()) {
                if (childIndex == rootIndex) continue;

                final Node child = new Node(nodes[childIndex], nodes);
                this.children.add(child);
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
            return children.stream().filter(n -> n.getName() == null ? null : n.getName().equals(name)).findFirst().orElse(null);
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
