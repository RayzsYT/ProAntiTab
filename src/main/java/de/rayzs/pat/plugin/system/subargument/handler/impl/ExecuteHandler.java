package de.rayzs.pat.plugin.system.subargument.handler.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.system.subargument.handler.SubArgumentHandler;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.plugin.system.subargument.argument.Arguments;

import java.util.ArrayList;
import java.util.List;

public class ExecuteHandler extends SubArgumentHandler {

    public ExecuteHandler(SubArgument instance) {
        super(instance);
    }

    public boolean handleCommandExecution(CommandSender sender, String command) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender is null!");
        }

        command = command.startsWith("/")
                ? command.substring(1)
                : command;

        return isBlocked(sender, command);
    }

    private boolean isBlocked(CommandSender sender, String command) {
        final Arguments arguments = getInstance().getPlayerArgument(sender);

        final List<String> commands = new ArrayList<>(arguments.CHAT_ARGUMENTS.getGeneralArgument().getInputs());
        final String firstArgument = StringUtils.getFirstArg(command);

        final String blockBaseCommandStr = firstArgument + " -_";
        final boolean blockBaseCommand = commands.contains(blockBaseCommandStr);

        if (!command.contains(" ")) {
            return blockBaseCommand;
        }

        if (blockBaseCommand) {
            commands.remove(blockBaseCommandStr);
        }


        final boolean inCommands = commands.stream().anyMatch(c -> StringUtils.getFirstArg(c).equalsIgnoreCase(firstArgument));
        final boolean negationInCommands = commands.stream().anyMatch(c -> StringUtils.getFirstArg(c).equalsIgnoreCase("!" + firstArgument));


        if (!inCommands && !negationInCommands) {
            return false;
        }


        boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;
        boolean listed = isListed(command, commands);
        boolean negated = isListed("!" + command, commands);

        if (!listed && !inCommands) {
            listed = turn;
        }

        if (negated) {
            listed = !turn;
        }

        return turn != listed;
    }

    private boolean isListed(String command, List<String> commands) {
        final String unmodifiable = command;
        final String firstArgUnmodifiable = StringUtils.getFirstArg(unmodifiable);


        commands = commands.stream().map(c -> {
            if (c.contains("%hidden")) {
                return c.replace("%hidden_", "%");
            }

            return c;
        }).toList();


        List<String> placeholderCommands = commands.stream().filter(s ->
                StringUtils.getFirstArg(s).equalsIgnoreCase(firstArgUnmodifiable) && s.contains("%")
        ).toList();

        if (!placeholderCommands.isEmpty()) {
            command = getInstance().replaceValuesWithPlaceholders(command);
        }



        boolean listed = false;
        for (String c : commands) {
            String cpyCommand = command;

            if (listed) break;

            if (cpyCommand.contains("%both_players%")) {
                if (c.contains("%online_players%"))
                    cpyCommand = StringUtils.replace(command, "%both_players%", "%online_players%");
                if (c.contains("%players%"))
                    cpyCommand = StringUtils.replace(command, "%both_players%", "%players%");
            }

            if (c.contains("%offline_players%")) {

                final String[] cSplit = c.split(" ");
                final String[] cpyCommandSplit = cpyCommand.split(" ");

                for (int i = 0; i < cSplit.length; i++) {
                    if (i >= cpyCommandSplit.length) {
                        break;
                    }

                    if (cSplit[i].equals("%offline_players%") && !cpyCommandSplit[i].endsWith("players%")) {
                        cpyCommandSplit[i] = "%offline_players%";
                    }
                }

                cpyCommand = String.join(" ", cpyCommandSplit);
            }

            boolean ends = c.endsWith("_-");
            if (ends) {
                c = StringUtils.replace(c, "_-", "");

                if (c.endsWith(" "))
                    c = StringUtils.replaceLast(c, " ", "");
            }

            listed = c.equalsIgnoreCase(cpyCommand) || cpyCommand.startsWith(c + " ");

            if (listed && ends && cpyCommand.length() > c.length()) {
                listed = false;
                break;
            }
        }

        return listed;
    }
}
