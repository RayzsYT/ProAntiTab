package de.rayzs.pat.utils.message;

import de.rayzs.pat.utils.CommandSender;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.translators.BukkitMessageTranslator;
import de.rayzs.pat.utils.message.translators.VelocityMessageTranslator;

import java.util.HashMap;
import java.util.Map;

public class MessageTranslator {

    private static final HashMap<Character, String> colors = new HashMap<>();
    private static final boolean support;
    private static Translator translator = null;

    static {
        colors.put('1', "<dark_blue>");
        colors.put('2', "<dark_green>");
        colors.put('3', "<dark_aqua>");
        colors.put('4', "<dark_red>");
        colors.put('5', "<dark_purple>");
        colors.put('6', "<orange>");
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
        colors.put('n', "<r>");
        colors.put('o', "<i>");
        colors.put('l', "<b>");

        support =  Reflection.getMinor() == 16 && Reflection.getRelease() == 5 || Reflection.getMinor() >= 17 || Reflection.isVelocityServer() || Reflection.getVersionName().contains("Paper");
        if(support) translator = Reflection.isVelocityServer() ? new VelocityMessageTranslator() : new BukkitMessageTranslator();
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

    public static boolean isSupported() {
        return support;
    }
}
