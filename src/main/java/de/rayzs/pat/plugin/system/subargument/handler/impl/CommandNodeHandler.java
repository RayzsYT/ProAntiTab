package de.rayzs.pat.plugin.system.subargument.handler.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.system.subargument.handler.SubArgumentHandler;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.node.BukkitCommandNodeHelper;
import de.rayzs.pat.utils.node.ProxyCommandNodeHelper;
import de.rayzs.pat.plugin.system.subargument.argument.ArgumentSource;
import de.rayzs.pat.plugin.system.subargument.argument.Arguments;

import java.util.ArrayList;
import java.util.List;

public class CommandNodeHandler extends SubArgumentHandler {

    public CommandNodeHandler(SubArgument instance) {
        super(instance);
    }

    public void handleCommandNode(ProxyCommandNodeHelper helper, Arguments playerArguments) {
        final boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;

        if (playerArguments == null) {
            return;
        }

        final ArgumentSource source = playerArguments.TAB_ARGUMENTS;
        if (source == null) {
            return;
        }

        final List<String> inputs = source.getAllInputs();
        final List<String> actuallyExistingCommands = helper.getChildrenNames();

        if (turn) {
            List<String> allEntries = inputs.stream().map(StringUtils::getFirstArg).toList();
            helper.removeIf(allEntries::contains);
        }

        final int length = inputs.size();
        for (int i = 0; i < length; i++) {
            String input;

            try {
                input = inputs.get(i);
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                break;
            }

            final boolean negated = Storage.Blacklist.BlockTypeFetcher.isNegated(input);

            if (!negated) {

                if (turn && actuallyExistingCommands.contains(StringUtils.getFirstArg(input))) {
                    helper.add(input, true);
                } else helper.removeSubArguments(input);

                continue;
            }

            final Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(input);
            if (type == Storage.Blacklist.BlockType.CHAT) {
                continue;
            }

            input = Storage.Blacklist.BlockTypeFetcher.cleanse(input);

            if (turn) helper.removeSubArguments(input);
            else if (actuallyExistingCommands.contains(StringUtils.getFirstArg(input))) {
                helper.add(input, true);
            }
        }
    }

    public void handleCommandNode(BukkitCommandNodeHelper helper, Arguments playerArguments) throws Exception {
        final boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;

        if (playerArguments == null) {
            return;
        }

        ArgumentSource source = playerArguments.TAB_ARGUMENTS;
        if (source == null) {
            return;
        }

        final List<String> inputs = new ArrayList<>(source.getAllInputs());
        final List<String> argumentsList = new ArrayList<>();

        final int length = inputs.size();
        for (int i = 0; i < length; i++) {
            String input;

            try {
                input = inputs.get(i);
            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                break;
            }

            final Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(input);
            if (type == Storage.Blacklist.BlockType.CHAT) {
                continue;
            }

            final boolean negated = Storage.Blacklist.BlockTypeFetcher.isNegated(input);
            input = Storage.Blacklist.BlockTypeFetcher.cleanse(input);

            if (!negated) {

                if (turn) argumentsList.add(input);
                else helper.removeSubArguments(input);

                continue;
            }

            if (turn) helper.removeSubArguments(input);
            else argumentsList.add(input);
        }

        if (argumentsList.isEmpty()) {
            return;
        }

        helper.spareRecursively(argumentsList);
    }
}
