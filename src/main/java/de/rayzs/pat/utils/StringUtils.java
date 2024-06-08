package de.rayzs.pat.utils;

public class StringUtils {

    public static String replaceFirst(String input, String trigger, String replacement) {
        if (input.contains(trigger)) {
            String[] split = input.split(trigger);
            if (split.length >= 1) {
                int pointAfter = split[0].length() + trigger.length();
                input = split[0] + replacement + input.substring(pointAfter);
            } else input = input.replace(trigger, replacement);
        }

        return input;
    }

    public static String replace(String input, String... triggers) {
        for (String trigger : triggers) {
            if(!input.contains(trigger)) continue;
            input = input.replace(trigger, "");
        }

        return input;
    }

    public static String getFirstArg(String input) {
        if(input.contains(" ")) {
            String[] split = input.split(" ");
            if(split.length > 0) input = split[0];
        }

        return input;
    }
}
