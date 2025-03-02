package de.rayzs.pat.utils.message.replacer;

import de.rayzs.pat.utils.message.replacer.impl.*;
import de.rayzs.pat.api.storage.Storage;
import java.util.function.Consumer;

public class PlaceholderReplacer {

    private static BukkitPlaceholderReplacer BUKKIT_PLACEHOLDER_REPLACER = null;
    private static ProxyPlaceholderReplacer PROXY_PLACEHOLDER_REPLACER = null;

    public static String replace(Object playerObj, String text) {
        if(!Storage.USE_PLACEHOLDERAPI) return text;
        if(!text.contains("%")) return text;
        if(BUKKIT_PLACEHOLDER_REPLACER == null) BUKKIT_PLACEHOLDER_REPLACER = new BukkitPlaceholderReplacer();

        return BUKKIT_PLACEHOLDER_REPLACER.process(playerObj, text);
    }

    public static boolean process(Object playerObj, String text, Consumer<String> consumer) {
        if(!Storage.USE_PAPIPROXYBRIDGE) return false;
        if(!text.contains("%")) return false;
        if(PROXY_PLACEHOLDER_REPLACER == null) PROXY_PLACEHOLDER_REPLACER = new ProxyPlaceholderReplacer();

        return PROXY_PLACEHOLDER_REPLACER.process(playerObj, text, consumer);
    }
}
