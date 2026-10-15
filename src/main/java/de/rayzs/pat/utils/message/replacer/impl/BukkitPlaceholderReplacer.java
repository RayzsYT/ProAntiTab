package de.rayzs.pat.utils.message.replacer.impl;

import de.rayzs.pat.utils.sender.CommandSender;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class BukkitPlaceholderReplacer {

    public String process(CommandSender sender, String text) {

        if (sender.isPlayer() && sender.getSenderObject() instanceof Player player) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }

        return PlaceholderAPI.setPlaceholders(null, text);
    }
}
