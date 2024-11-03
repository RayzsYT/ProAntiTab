package de.rayzs.pat.addon.events;

import de.rayzs.pat.addon.utils.Responses;
import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.addon.utils.Argument;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.utils.*;

public class ExecuteCommand extends ExecuteCommandEvent {

    @Override
    public void handle(ExecuteCommandEvent event) {
        String command = StringUtils.replaceFirst(event.getCommand(), "/", "");
        if (!command.contains(" ")) return;

        if (shouldCommandBeBlocked(event, command)) {
            event.setBlocked(true);
            event.setCancelled(true);

            MessageTranslator.send(event.getSenderObj(), Responses.getResponse(event.getCommand()), "%command%", event.getCommand());
        }
    }

    private boolean shouldCommandBeBlocked(ExecuteCommandEvent event, String command) {
        CommandSender sender = new CommandSender(event.getSenderObj());
        boolean listed = false,
                turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED,
                blocked = event.isBlocked(),
                ignored = false,
                useFilter = false,
                tooBig = false;

        String[] split, originCommandSplit, copiedOriginCommandSplit;
        String tmpCommand;
        for (String s : SubArgsAddon.PLAYER_COMMANDS.getOrDefault(sender.getUniqueId(), Argument.getGeneralArgument()).getInputs()) {
            tmpCommand = command;

            if(s.split(" ")[0].equalsIgnoreCase(tmpCommand.split(" ")[0]))
                useFilter = true;

            originCommandSplit = tmpCommand.split(" ");
            split = s.split(" ");
            int i;

            for(i = 0; i < split.length; i++) {
                if(!split[i].equals("%online_players%")) continue;

                boolean foundPlayer = false;
                for (String playerName : SubArgsAddon.getPlayerNames()) {
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
