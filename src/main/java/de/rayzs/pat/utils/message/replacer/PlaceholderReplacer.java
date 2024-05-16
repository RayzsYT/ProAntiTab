package de.rayzs.pat.utils.message.replacer;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderReplacer {

    public static String replace(Object player, String text) {
        if(!(player instanceof Player)) return text;
        return PlaceholderAPI.setPlaceholders((Player) player, text);
    }
}
