package de.rayzs.pat.plugin.modules.subargs.events;

import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.subargs.*;
import de.rayzs.pat.utils.*;

public class ExecuteCommand extends ExecuteCommandEvent {

    @Override
    public void handle(ExecuteCommandEvent event) {
        String command = StringUtils.replaceFirst(event.getCommand(), "/", "");
        CommandSender sender = new CommandSender(event.getSenderObj());

        /* Removed due to the fact that it is not required after all.
        if(PermissionUtil.hasBypassPermission(event.getSenderObj(), command.contains(" ") ? command.split(" ")[0] : command))
            return;
        */

        if(command.contains(" ") && shouldCommandBeBlocked(sender, event, command)) {
            event.setBlocked(true);
            event.setCancelled(true);
            MessageTranslator.send(event.getSenderObj(), ResponseHandler.getResponse(sender.getUniqueId(), event.getCommand()), "%command%", event.getCommand());
            return;
        }

        if(!event.isBlocked()) return;

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command))
            return;

        event.setCancelled(true);
        MessageTranslator.send(event.getSenderObj(), ResponseHandler.getResponse(sender.getUniqueId(), event.getCommand(), Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE.getLines()), "%command%", event.getCommand());
    }

    private boolean shouldCommandBeBlocked(CommandSender sender, ExecuteCommandEvent event, String command) {
        boolean listed = false,
                turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED,
                blocked = event.isBlocked(),
                ignored = false,
                useFilter = false,
                tooBig = false;

        String[] split, originCommandSplit, copiedOriginCommandSplit;
        String tmpCommand;
        for (String s : SubArgsModule.PLAYER_COMMANDS.getOrDefault(sender.getUniqueId(), Argument.getGeneralArgument()).getInputs()) {
            if(s.contains("%hidden_online_players%")) continue;
            tmpCommand = command;

            if(s.split(" ")[0].equalsIgnoreCase(tmpCommand.split(" ")[0]))
                useFilter = true;

            originCommandSplit = tmpCommand.split(" ");
            split = s.split(" ");
            int i;

            for(i = 0; i < split.length; i++) {
                if(!split[i].equals("%online_players%")) continue;

                boolean foundPlayer = false;
                for (String playerName : SubArgsModule.getPlayerNames()) {
                    if(i >= originCommandSplit.length) continue;
                    if(!playerName.equalsIgnoreCase(originCommandSplit[i])) continue;

                    foundPlayer = true;
                    originCommandSplit[i] = "%online_players%";
                    break;
                }

                if(!foundPlayer) {

                    copiedOriginCommandSplit = originCommandSplit.clone();
                    copiedOriginCommandSplit[copiedOriginCommandSplit.length-1] = null;
                    String c = String.join(" ", copiedOriginCommandSplit);

                    if(s.startsWith(c)) return turn;
                }

                tmpCommand = String.join(" ", originCommandSplit);
            }

            if (!tmpCommand.toLowerCase().startsWith(StringUtils.replaceFirst(s.toLowerCase(), " _-", "")))
                continue;

            if (!listed) listed = true;
            if (s.endsWith(" _-")) ignored = true;

            tooBig = originCommandSplit.length > s.replace(" _-", "").split(" ").length;
        }

        return !(blocked || !useFilter) && turn != listed || ignored && tooBig;
    }
}
