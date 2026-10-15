package de.rayzs.pat.utils.message.replacer;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.replacer.impl.*;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.Objects;
import java.util.function.Consumer;

public class PlaceholderReplacer {

    private static BukkitPlaceholderReplacer BUKKIT_PLACEHOLDER_REPLACER = null;
    private static ProxyPlaceholderReplacer PROXY_PLACEHOLDER_REPLACER = null;

    public static String applyPlaceholders(CommandSender sender, String text, String... replacements) {
        text = applyGeneralReplacements(sender, text, replacements);

        if (Reflection.isProxyServer() || !Storage.USE_PLACEHOLDERAPI)
            return text;

        if (!text.contains("%"))
            return text;

        if (BUKKIT_PLACEHOLDER_REPLACER == null)
            BUKKIT_PLACEHOLDER_REPLACER = new BukkitPlaceholderReplacer();

        return BUKKIT_PLACEHOLDER_REPLACER.process(sender, text);
    }

    public static void applyProxyPlaceholders(CommandSender sender, String text, Consumer<String> consumer, String... replacements) {
        text = applyPlaceholders(sender, text, replacements);

        if (!Storage.USE_PAPIPROXYBRIDGE) {
            consumer.accept(text);
            return;
        }

        if (!text.contains("%")) {
            consumer.accept(text);
            return;
        }

        if (PROXY_PLACEHOLDER_REPLACER == null) {
            PROXY_PLACEHOLDER_REPLACER = new ProxyPlaceholderReplacer();
        }

        if (!PROXY_PLACEHOLDER_REPLACER.process(sender, text, consumer)) {
            consumer.accept(text);
        }
    }

    private static String applyGeneralReplacements(CommandSender sender, String text, String... replacements) {
        final String executorName = sender == null ? "" : sender.getName();

        final String token = (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED || Reflection.isProxyServer())
                ? Storage.CENSORED_TOKEN
                : "/";

        final String serverName = Objects.requireNonNullElse(Storage.SERVER_NAME, "/");

        return StringUtils.replace(StringUtils.replace(text, replacements),
                "&", "§",
                "\\n", "\n",
                "%executor%", executorName,
                "%prefix%", Storage.ConfigSections.Messages.PREFIX.PREFIX,
                "%pat_general_prefix%", Storage.ConfigSections.Messages.PREFIX.PREFIX,
                "%token%", token,
                "%sync_server_name%", serverName,
                "%server%", serverName,
                "%current_version%", Storage.CURRENT_VERSION,
                "%newest_version%", Storage.NEWER_VERSION
        );
    }
}
