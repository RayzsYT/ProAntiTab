package de.rayzs.pat.utils.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.*;
import de.rayzs.pat.api.storage.Storage;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProxyCommandNodeHelper<T> {

    private static final Command DUMMY_COMMAND = (context) -> 0;

    private static SuggestionProvider DEFAULT_SUGGESTION_PROVIDER;

    public static void setDefaultSuggestionProvider(SuggestionProvider<?> suggestionProvider) {
        DEFAULT_SUGGESTION_PROVIDER = suggestionProvider;
    }

    public static CommandNode createDummyCommandNode(String command) {
        return LiteralArgumentBuilder
                .literal(command)
                .executes(DUMMY_COMMAND)
                .then(RequiredArgumentBuilder
                        .argument("args", (ArgumentType) StringArgumentType.greedyString())
                        .suggests(DEFAULT_SUGGESTION_PROVIDER)
                        .executes(DUMMY_COMMAND))
                .build();
    }

    public static CommandNode createEmptyDummyCommandNode(String command) {
        return LiteralArgumentBuilder
                .literal(command)
                .executes(DUMMY_COMMAND)
                .then(RequiredArgumentBuilder
                        .argument("args", (ArgumentType) StringArgumentType.greedyString())
                        .executes(DUMMY_COMMAND))
                .build();
    }

    private final RootCommandNode<T> rootNode;

    public ProxyCommandNodeHelper(RootCommandNode<T> rootNode) {
        this.rootNode = rootNode;
    }

    public List<String> getChildrenNames() {
        return rootNode.getChildren().stream().map(CommandNode::getName).collect(Collectors.toList());
    }

    public ProxyCommandNodeHelper<T> removeIf(CommandNode<T> node, Predicate<String> predicate) {
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

    public ProxyCommandNodeHelper<T> removeIf(String childName, Predicate<String> predicate) {
        CommandNode<T> node = rootNode.getChild(childName);
        return removeIf(node, predicate);
    }

    public ProxyCommandNodeHelper<T> removeIf(Predicate<String> predicate) {
        return removeIf(rootNode, predicate);
    }

    public ProxyCommandNodeHelper<T> clear() {
        rootNode.getChildren().clear();
        return this;
    }

    public ProxyCommandNodeHelper<T> clear(String childName) {
        CommandNode child = rootNode.getChild(childName);

        if (child != null) {
            child.getChildren().clear();
        }

        return this;
    }


    public ProxyCommandNodeHelper<T> remove(Object obj) {
        rootNode.getChildren().remove(obj);
        return this;
    }

    public ProxyCommandNodeHelper<T> remove(CommandNode node, Object obj) {
        node.getChildren().remove(obj);
        return this;
    }

    public ProxyCommandNodeHelper<T> add(String original, boolean autoSuggestions) {
        String[] args = original.split(" ");
        add(original, rootNode, args, 0, autoSuggestions);
        return this;
    }

    public ProxyCommandNodeHelper<T> removeSubArguments(String original) {
        String[] args = original.split(" ");
        removeSubArguments(rootNode, args, 0);
        return this;
    }

    private void removeSubArguments(CommandNode<T> parent, String[] args, int index) {
        if (index >= args.length) return;

        if (parent == null) {
            return;
        }

        String part = args[index];
        CommandNode<T> childNode = parent.getChild(part);

        if (index == (args.length - 1)) {
            parent.getChildren().remove(childNode);
            return;
        }

        removeSubArguments(childNode, args, index + 1);
    }

    private void add(final String original, CommandNode<T> parent, String[] args, int index, boolean autoSuggestions) {
        if (index >= args.length) {
            return;
        }

        String part = args[index];

        if (index == 0)
            part = Storage.Blacklist.BlockTypeFetcher.modify(Storage.Blacklist.BlockTypeFetcher.modify(part));

        String next = index + 1 >= args.length ? null : args[index + 1];
        CommandNode<T> childNode = parent.getChild(part);

        if (original.charAt(0) == '!' && index == (args.length - 1)) {
             return;
        }

        if (childNode == null) {
            LiteralCommandNode newNode;

            final boolean cancel = next != null && (next.equals("_-"));
            final boolean stop = next != null && next.startsWith("%") && next.endsWith("%");
            final boolean skip = part.equals("-_");

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
                                .suggests(DEFAULT_SUGGESTION_PROVIDER))
                        .build();
            }

            parent.addChild(newNode);

            if (stop || cancel) {
                return;
            }

            childNode = newNode;
        }

        add(original, childNode, args, index + 1, autoSuggestions);
    }
}