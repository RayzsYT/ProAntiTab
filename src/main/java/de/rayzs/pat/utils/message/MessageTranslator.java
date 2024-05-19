package de.rayzs.pat.utils.message;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.message.translators.*;
import java.util.*;

public class MessageTranslator {

    private static final HashMap<Character, String> colors = new HashMap<>();
    private static boolean support;
    private static Translator translator = null;
    private static boolean placeholderSupport = false;

    public static void initialize() {
        colors.put('1', "<dark_blue>");
        colors.put('2', "<dark_green>");
        colors.put('3', "<dark_aqua>");
        colors.put('4', "<dark_red>");
        colors.put('5', "<dark_purple>");
        colors.put('6', "<gold>");
        colors.put('7', "<gray>");
        colors.put('8', "<dark_gray>");
        colors.put('9', "<blue>");
        colors.put('a', "<green>");
        colors.put('b', "<aqua>");
        colors.put('c', "<red>");
        colors.put('d', "<light_purple>");
        colors.put('e', "<yellow>");
        colors.put('f', "<white>");
        colors.put('m', "<st>");
        colors.put('k', "<obf>");
        colors.put('n', "<reset>");
        colors.put('o', "<i>");
        colors.put('l', "<b>");

        support =  Reflection.getMinor() >= 17
                || Reflection.isProxyServer();

        if(support)
            translator = Reflection.isVelocityServer() ? new VelocityMessageTranslator()
                    : Reflection.isProxyServer() ? new BungeeMessageTranslator()
                    : new BukkitMessageTranslator();
    }

    public static void enablePlaceholderSupport() {
        placeholderSupport = true;
    }

    public static String replaceMessage(String text) {
        return replaceMessage(null, text);
    }

    public static String replaceMessage(Object playerObj, String text) {
        text = text.replace("&", "§").replace("%current_version%", Storage.CURRENT_VERSION).replace("%newest_version%", Storage.NEWER_VERSION).replace("\\n", "\n");
        return !placeholderSupport || playerObj == null || Reflection.isProxyServer() ? text : PlaceholderReplacer.replace(playerObj, text);
    }

    public static String translateLegacy(String text) {
        text = replaceMessage(text);
        if(!text.contains("§")) return text;

        for (Map.Entry<Character, String> entry : colors.entrySet())
            text = text.replace("§" + entry.getKey(), entry.getValue());

        return text;
    }

    public static String colorless(String text) {
        text = replaceMessage(text);
        if(!text.contains("§")) return text;

        for (Map.Entry<Character, String> entry : colors.entrySet())
            text = text.replace("§" + entry.getKey(), "");

        return text;
    }

    public static void send(Object target, List<String> texts) {
        texts.forEach(text -> send(target, text));
    }

    public static void send(Object target, String text) {
        text = replaceMessage(target, text);
        if(translator == null) {
            CommandSender sender = target instanceof CommandSender ? (CommandSender) target : new CommandSender(target);
            sender.sendMessage(text);
            return;
        }

        translator.send(target, text);
    }

    public static String translateIntoMiniMessage(String text) {
        return translator.translate(replaceMessage(text));
    }

    public static void closeAudiences() {
        if(translator == null) return;
        translator.close();
    }

    public static boolean isSupported() {
        return support;
    }
}
