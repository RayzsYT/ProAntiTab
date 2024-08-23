package de.rayzs.pat.addon.events;

import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.addon.SubArgsAddon;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class BukkitExecuteCommand extends ExecuteCommandEvent {

    @Override
    public void handle(ExecuteCommandEvent event) {
        Player player = Bukkit.getPlayer(event.getSenderUniqueId());

        String command = StringUtils.replaceFirst(event.getCommand(), "/", "");
        boolean listed = false, spaces = false, equals = false,
                turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED,
                blocked = event.isBlocked(),
                ignored = false;

        for (String s : SubArgsAddon.GENERAL_LIST) {

            if (s.endsWith(" _-")) {
                s = StringUtils.replaceFirst(command, " _-", "");
                ignored = true;
            }

            if (!equals && s.equalsIgnoreCase(command))
                equals = true;

            if (!spaces && s.contains(" ") && command.contains(" "))
                spaces = true;

            if (!command.toLowerCase().startsWith(s.toLowerCase())) continue;
            listed = true;
        }

        if (spaces) {

            if (turn && !blocked && (!(listed || equals) || ignored)) {
                event.setBlocked(true);
                event.setCancelled(true);

                MessageTranslator.send(player, SubArgsAddon.BLOCKED_MESSAGE, "%command%", event.getCommand());
            }

            if (!turn && !blocked && ((listed || equals) || ignored)) {
                event.setBlocked(true);
                event.setCancelled(true);

                MessageTranslator.send(player, SubArgsAddon.BLOCKED_MESSAGE, "%command%", event.getCommand());
            }
        }
    }
}
