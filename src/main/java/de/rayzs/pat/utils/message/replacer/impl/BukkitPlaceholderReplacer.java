package de.rayzs.pat.utils.message.replacer.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import de.rayzs.pat.utils.CommandSender;
import org.bukkit.entity.Player;

public class BukkitPlaceholderReplacer {

    public String process(Object playerObj, String text) {
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
