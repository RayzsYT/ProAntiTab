package de.rayzs.pat.utils.message.replacer;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.message.replacer.impl.*;
import java.util.function.Consumer;

public class PlaceholderReplacer {

    public static String replace(Object playerObj, String text) {
        if(!Storage.USE_PLACEHOLDERAPI) return text;
        return BukkitPlaceholderReplacer.process(playerObj, text);
    }

    public static boolean process(Object playerObj, String text, Consumer<String> consumer) {
        if(!Storage.USE_PAPIPROXYBRIDGE) return false;
        return ProxyPlaceholderReplacer.process(playerObj, text, consumer);
    }
}
