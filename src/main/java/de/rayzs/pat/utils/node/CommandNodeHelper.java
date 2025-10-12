package de.rayzs.pat.utils.node;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.*;
import net.md_5.bungee.protocol.packet.Commands;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommandNodeHelper<T> {

    private final RootCommandNode<T> rootNode;

    public CommandNodeHelper(RootCommandNode<T> rootNode) {
        this.rootNode = rootNode;
    }

    public List<String> getChildrenNames() {
        return rootNode.getChildren().stream().map(CommandNode::getName).collect(Collectors.toList());
    }

    public CommandNodeHelper<T> removeIf(CommandNode<T> node, Predicate<String> predicate) {
        if (node != null && node.getRedirect() != null)
            node = node.getRedirect();

        List<Object> objs = new ArrayList<>();

        for (CommandNode<T> n : node.getChildren()) {
            if (n == null)
                continue;

            String name = n.getName();
            if (predicate.test(name)) {
                objs.add(n);
            }
        }

        for (Object obj : objs) {
            node.getChildren().remove(obj);
        }

        return this;
    }

    public CommandNodeHelper<T> removeIf(String childName, Predicate<String> predicate) {
        CommandNode<T> node = rootNode.getChild(childName);
        return removeIf(node, predicate);
    }

    public CommandNodeHelper<T> removeIf(Predicate<String> predicate) {
        return removeIf(rootNode, predicate);
    }

    public CommandNodeHelper<T> clear() {
        rootNode.getChildren().clear();
        return this;
    }

    public CommandNodeHelper<T> clear(String childName) {
        CommandNode child = rootNode.getChild(childName);

        if (child != null) {
            child.getChildren().clear();
        }

        return this;
    }


    public CommandNodeHelper<T> remove(Object obj) {
        rootNode.getChildren().remove(obj);
        return this;
    }

    public CommandNodeHelper<T> remove(CommandNode node, Object obj) {
        node.getChildren().remove(obj);
        return this;
    }

    public CommandNodeHelper<T> add(String commandPath, boolean autoSuggestions) {
        String[] args = commandPath.split(" ");
        add(rootNode, args, 0, autoSuggestions);
        return this;
    }

    private void add(CommandNode<T> parent, String[] args, int index, boolean autoSuggestions) {
        if (index >= args.length) return;

        String part = args[index];
        String next = index + 1 >= args.length ? null : args[index + 1];
        CommandNode<T> childNode = parent.getChild(part);

        if (childNode == null) {
            LiteralCommandNode newNode;

            boolean cancel = next != null && (next.equals("_-"));
            boolean stop = next != null && next.startsWith("%") && next.endsWith("%");
            boolean skip = part.equals("-_");

            if (skip) {
                return;
            }

            if (!autoSuggestions || cancel || !stop && index != args.length - 1) {
                newNode = LiteralArgumentBuilder.literal(part)
                        .then(RequiredArgumentBuilder
                                .argument("args", StringArgumentType.string()))
                        .build();
            } else {
                newNode = LiteralArgumentBuilder.literal(part)
                        .then(RequiredArgumentBuilder
                                .argument("args", (ArgumentType) StringArgumentType.greedyString())
                                .suggests(Commands.SuggestionRegistry.ASK_SERVER))
                        .build();
            }

            parent.addChild(newNode);

            if (stop || cancel) {
                return;
            }

            childNode = newNode;
        }

        add(childNode, args, index + 1, autoSuggestions);
    }
}