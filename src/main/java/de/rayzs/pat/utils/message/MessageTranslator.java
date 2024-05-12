package de.rayzs.pat.utils.message;

import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.translators.BukkitMessageTranslator;
import de.rayzs.pat.utils.message.translators.BungeeMessageTranslator;
import de.rayzs.pat.utils.message.translators.VelocityMessageTranslator;

import java.util.HashMap;
import java.util.Map;

public class MessageTranslator {

    private static final HashMap<Character, String> colors = new HashMap<>();
    private static boolean support;
    private static Translator translator = null;

    public static void initialize() {
        final String reset = "<r>";
        colors.put('1', reset + "<dark_blue>");
        colors.put('2', reset + "<dark_green>");
        colors.put('3', reset + "<dark_aqua>");
        colors.put('4', reset + "<dark_red>");
        colors.put('5', reset + "<dark_purple>");
        colors.put('6', reset + "<orange>");
        colors.put('7', reset + "<gray>");
        colors.put('8', reset + "<dark_gray>");
        colors.put('9', reset + "<blue>");
        colors.put('a', reset + "<green>");
        colors.put('b', reset + "<aqua>");
        colors.put('c', reset + "<red>");
        colors.put('d', reset + "<light_purple>");
        colors.put('e', reset + "<yellow>");
        colors.put('f', reset + "<white>");
        colors.put('m', "<st>");
        colors.put('k', "<obf>");
        colors.put('n', reset);
        colors.put('o', "<i>");
        colors.put('l', "<b>");

        support =  Reflection.getMinor() == 16 && Reflection.getRelease() == 5
                || Reflection.getMinor() >= 17
                || Reflection.isProxyServer();

        if(support)
            translator = Reflection.isVelocityServer() ? new VelocityMessageTranslator()
                    : Reflection.isProxyServer() ? new BungeeMessageTranslator()
                    : new BukkitMessageTranslator();
    }

    public static String translateLegacy(String text) {
        text = text.replace("&", "§");
        if(!text.contains("§")) return text;

        for (Map.Entry<Character, String> entry : colors.entrySet())
            text = text.replace("§" + entry.getKey(), entry.getValue());

        return text;
    }

    public static String clean(String text) {
        text = text.replace("&", "§");
        if(!text.contains("§")) return text;

        for (Map.Entry<Character, String> entry : colors.entrySet())
            text = text.replace("§" + entry.getKey(), "");

        return text;
    }

    public static void send(Object target, String text) {
        if(translator == null) {
            CommandSender sender = target instanceof CommandSender ? (CommandSender) target : new CommandSender(target);
            sender.sendMessage(text);
            return;
        }

        translator.send(target, text);
    }

    public static void closeAudiences() {
        if(translator == null) return;
        translator.close();
    }

    public static boolean isSupported() {
        return support;
    }
}
