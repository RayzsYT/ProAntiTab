package de.rayzs.pat.utils.message;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.message.translators.*;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.sender.CommandSender;

import java.sql.Ref;
import java.util.List;

public class MessageHelper {

    private static boolean support;
    private static Translator translator = null;

    public static void initialize() {

        if (Reflection.isBukkit() || Reflection.isSpigot()) {
            Logger.warning("MiniMessage support disabled! Your server software is too old. Please switch to a more modern paper-based fork such as Paper, Purpur, Leaf..., etc to enable MiniMessage support.");
            return;
        }

        if (Reflection.isBungeeServer() && !Reflection.doesClassExist("net.kyori.adventure.util.Buildable$Builder")) {
            Logger.warning("MiniMessage support disabled! Since MiniMessage isn't supported for Bungeecord anymore and this version of Bungeecord is too advanced, MiniMessage support is disabled!");
            return;
        }

        support = true;
        translator = Reflection.isVelocityServer() ? new VelocityMessageTranslator()
                : Reflection.isProxyServer() ? new BungeeMessageTranslator()
                : Reflection.isPaper() ? new PaperMessageTranslator()
                : new BukkitMessageTranslator();
    }

    public static void sendMessage(CommandSender sender, MultipleMessagesHelper texts, String... replacements) {
        sendMessage(sender, texts.getLines(), replacements);
    }

    public static void sendMessage(CommandSender sender, List<String> texts, String... replacements) {
        sendMessage(sender, StringUtils.getStringList(texts, "\n"), replacements);
    }

    public static void sendMessage(CommandSender sender, String text, String... replacements) {
        if (text.isEmpty()) return;


        if (!Reflection.isProxyServer()) {
            text = PlaceholderReplacer.applyPlaceholders(sender, text, replacements);

            if (support) {
                translator.send(sender, text);
                return;
            }

            sender.sendMessage(text);
            return;
        }


        if (support) {
            PlaceholderReplacer.applyProxyPlaceholders(sender, text, result -> translator.send(sender, result), replacements);
            return;
        }

        PlaceholderReplacer.applyProxyPlaceholders(sender, text, sender::sendMessage, replacements);
    }

    public static void sendActionbar(CommandSender sender, String message) {
        if (!support) {
            sender.sendActionbar(message);
            return;
        }

        translator.sendActionbar(sender, message);
    }

    public static void sendTitle(CommandSender sender, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (!support) {
            sender.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            return;
        }

        translator.sendTitle(sender, title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void playSound(CommandSender sender, String soundKey, float volume, float pitch) throws Exception {
        translator.playSound(sender, soundKey, volume, pitch);
    }

    public static String strip(String text) {
        return translator.strip(text);
    }

    public static void closeAudiences() {
        if (translator == null) return;
        translator.close();
    }

    public static boolean isSupported() {
        return support;
    }
}
