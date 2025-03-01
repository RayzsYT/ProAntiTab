package de.rayzs.pat.plugin.node;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.*;

public class CommandNodeHelper<T> {

    private final RootCommandNode<T> rootNode;

    public CommandNodeHelper(RootCommandNode<T> rootNode) {
        this.rootNode = rootNode;
    }

    public CommandNodeHelper<T> clear() {
        rootNode.getChildren().clear();
        return this;
    }

    public CommandNodeHelper<T> add(String commandPath) {
        String[] args = commandPath.split(" ");
        add(rootNode, args, 0);
        return this;
    }

    private void add(CommandNode<T> parent, String[] args, int index) {
        if (index >= args.length) return;

        String part = args[index];
        CommandNode<T> childNode = parent.getChild(part);

        if (childNode == null) {
            LiteralCommandNode<T> newNode = LiteralArgumentBuilder.<T>literal(part).build();
            parent.addChild(newNode);
            childNode = newNode;
        }

        add(childNode, args, index + 1);
    }
}