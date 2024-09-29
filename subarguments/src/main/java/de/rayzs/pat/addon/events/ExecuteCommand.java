package de.rayzs.pat.addon.events;

import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.utils.message.MessageTranslator;
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

            MessageTranslator.send(event.getSenderObj(), SubArgsAddon.BLOCKED_MESSAGE, "%command%", event.getCommand());
        }
    }

    private boolean shouldCommandBeBlocked(ExecuteCommandEvent event, String command) {
        boolean listed = false,
                turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED,
                blocked = event.isBlocked(),
                ignored = false,
                useFilter = false;

        for (String s : SubArgsAddon.GENERAL_LIST) {

            if(s.split(" ")[0].equalsIgnoreCase(command.split(" ")[0]))
                useFilter = true;

            if (!command.toLowerCase().startsWith(s.toLowerCase())) continue;
            if (!listed) listed = true;
            if (s.endsWith(" _-")) ignored = true;
        }

        return !(blocked || !useFilter) && turn != listed || ignored;
    }
}
