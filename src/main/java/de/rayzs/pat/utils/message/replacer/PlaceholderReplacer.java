package de.rayzs.pat.utils.message.replacer;

import de.rayzs.pat.utils.CommandSender;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderReplacer {

    public static String replace(Object playerObj, String text) {
        Player player = null;

        if(playerObj != null)
            if(playerObj instanceof CommandSender) {
                CommandSender sender = (CommandSender) playerObj;
                player = sender.isPlayer() ? (Player) sender.getSenderObject() : null;
            } else if(playerObj instanceof Player)
                player = (Player) playerObj;

        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
