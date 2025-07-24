package de.rayzs.pat.plugin.modules.subargs.events;

import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.subargs.*;
import de.rayzs.pat.utils.*;

import java.util.ArrayList;
import java.util.List;

public class ExecuteCommand extends ExecuteCommandEvent {

    @Override
    public void handle(ExecuteCommandEvent event) {
        String command = StringUtils.replaceFirst(event.getCommand(), "/", "");
        CommandSender sender = new CommandSender(event.getSenderObj());

        if (PermissionUtil.hasBypassPermission(event.getSenderObj(), StringUtils.getFirstArg(command)))
            return;

        if (command.contains(" ") && !canPlayerExecute(sender, command)) {
            event.setBlocked(true);
            event.setCancelled(true);
            MessageTranslator.send(event.getSenderObj(), ResponseHandler.getResponse(sender.getUniqueId(), event.getCommand()), "%command%", event.getCommand());
            return;
        }

        if(!event.isBlocked())
            return;

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command))
            return;

        event.setCancelled(true);
        MessageTranslator.send(event.getSenderObj(), ResponseHandler.getResponse(sender.getUniqueId(), event.getCommand(), Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE.getLines()), "%command%", event.getCommand());
    }

    private boolean canPlayerExecute(CommandSender sender, String command) {
        boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;
        boolean listed = false;

        Arguments arguments = SubArgsModule.PLAYER_COMMANDS.getOrDefault(sender.getUniqueId(), Arguments.ARGUMENTS);
        List<String> commands = new ArrayList<>(arguments.CHAT_ARGUMENTS.getGeneralArgument().getInputs());

        final String unmodifiable = command;
        if (commands.stream().noneMatch(c -> StringUtils.getFirstArg(c).equalsIgnoreCase(StringUtils.getFirstArg(unmodifiable)))) {
            return true;
        }

        commands = commands.stream().map(c -> {
            if (c.contains("%hidden")) {
                return c.replace("%hidden_", "%");
            }

            return c;
        }).toList();

        List<String> placeholderCommands = commands.stream().filter(s -> s.contains("%")).toList();

        if (!placeholderCommands.isEmpty())
            command = replacePlaceholders(command, placeholderCommands);

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

        return turn == listed;
    }

    private String replacePlaceholders(String input, List<String> abnormalCommands) {
        String[] split = input.split(" ");

        for (int i = 0; i < split.length; i++) {
            String s = split[i];

            boolean number = NumberUtils.isDigit(split[i]),
                    online = Storage.getLoader().isPlayerOnline(s),
                    general = Storage.getLoader().doesPlayerExist(s);

            if (number) {
                split[i] = "%numbers%";
                continue;
            }

            if (online && general) {
                split[i] = "%both_players%";
                continue;
            }

            if (online) {
                split[i] = "%online_players%";
                continue;
            }

            if (general)
                split[i] = "%players%";
        }

        return String.join(" ", split);
    }
}
