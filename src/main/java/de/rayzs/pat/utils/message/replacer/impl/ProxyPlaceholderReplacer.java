package de.rayzs.pat.utils.message.replacer.impl;

import de.rayzs.pat.utils.sender.CommandSender;
import net.william278.papiproxybridge.api.PlaceholderAPI;
import java.util.function.Consumer;
import de.rayzs.pat.utils.*;
import java.util.UUID;

public class ProxyPlaceholderReplacer {

    private final PlaceholderAPI placeholderAPI;

    public ProxyPlaceholderReplacer() {
        placeholderAPI = PlaceholderAPI.createInstance();
        placeholderAPI.setRequestTimeout(1500);
    }

    public boolean process(CommandSender sender, String text, Consumer<String> consumer) {
        if (sender == null || !sender.isPlayer()) return false;

        placeholderAPI.formatPlaceholders(text, sender.getUniqueId()).thenAccept(consumer);
        return true;
    }
}
