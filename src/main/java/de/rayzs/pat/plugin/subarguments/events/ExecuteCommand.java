package de.rayzs.pat.plugin.subarguments.events;

import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.plugin.subarguments.SubArguments;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import de.rayzs.pat.utils.subargs.*;
import de.rayzs.pat.utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExecuteCommand extends ExecuteCommandEvent {

    @Override
    public void handle(ExecuteCommandEvent event) {
        String command = event.getCommand();
        command = command.startsWith("/")
                ? command.substring(1)
                : command;

        final String displayCommand = StringUtils.replaceTriggers(
                command, "",
                "\\", "<", ">", "&"
        );

        CommandSender sender = CommandSenderHandler.from(event.getSenderObj());

        if (sender == null)
            return;

        UUID uuid = sender.getUniqueId();
        String serverName = sender.getServerName();
        String playerName = sender.getName();

        if (isBlocked(sender, command)) {
            event.setBlocked(true);
            event.setCancelled(true);

            MessageTranslator.send(event.getSenderObj(),
                    ResponseHandler.getResponse(uuid, playerName, serverName, event.getCommand()),
                    "%command%", displayCommand);

            return;
        }

        if (!event.isBlocked())
            return;

        event.setCancelled(true);

        MessageTranslator.send(
                event.getSenderObj(),
                ResponseHandler.getResponse(uuid, playerName, serverName, event.getCommand(), Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE.getLines()),
                "%command%", StringUtils.getFirstArg(displayCommand)
        );

    }

    private boolean isBlocked(CommandSender sender, String command) {
        final Arguments arguments = SubArguments.PLAYER_COMMANDS.getOrDefault(sender.getUniqueId(), Arguments.ARGUMENTS);
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


        boolean inCommands = commands.stream().anyMatch(c -> StringUtils.getFirstArg(c).equalsIgnoreCase(firstArgument));
        boolean negationInCommands = commands.stream().anyMatch(c -> StringUtils.getFirstArg(c).equalsIgnoreCase("!" + firstArgument));


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
        boolean listed = false;

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
            command = SubArguments.replacePlaceholders(command);
        }

        String cpyCommand;
        for (String c : commands) {
            cpyCommand = command;

            if (listed)
                break;

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
