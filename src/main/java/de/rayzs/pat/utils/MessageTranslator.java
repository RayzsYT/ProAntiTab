package de.rayzs.pat.utils;

import java.util.HashMap;
import java.util.Map;

public class MessageTranslator {

    private static final HashMap<Character, String> COLORS = new HashMap<>();

    static {
        COLORS.put('1', "<dark_blue>");
        COLORS.put('2', "<dark_green>");
        COLORS.put('3', "<dark_aqua>");
        COLORS.put('4', "<dark_red>");
        COLORS.put('5', "<dark_purple>");
        COLORS.put('6', "<orange>");
        COLORS.put('7', "<gray>");
        COLORS.put('8', "<dark_gray>");
        COLORS.put('9', "<blue>");
        COLORS.put('a', "<green>");
        COLORS.put('b', "<aqua>");
        COLORS.put('c', "<red>");
        COLORS.put('d', "<light_purple>");
        COLORS.put('e', "<yellow>");
        COLORS.put('f', "<white>");
        COLORS.put('m', "<st>");
        COLORS.put('k', "<obf>");
        COLORS.put('n', "<r>");
        COLORS.put('o', "<i>");
        COLORS.put('l', "<b>");
    }

    public static String translate(String text) {
        if(!Reflection.isVelocityServer()) return text.replace("&", "§");
        if(!text.contains("§")) return text;

        for (Map.Entry<Character, String> entry : COLORS.entrySet())
            text = text.replace("§" + entry.getKey(), entry.getValue());

        return text;
    }

    public static String clear(String text) {
        for (Map.Entry<Character, String> entry : COLORS.entrySet())
            text = text.replace("§" + entry.getKey(), "");

        return text;
    }

}
