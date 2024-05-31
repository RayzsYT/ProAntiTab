package de.rayzs.pat.utils;

public class StringUtils {

    public static String replaceFirst(String input, String trigger, String replacement) {
        if(input.contains(trigger)) {
            String[] split = input.split(trigger);
            if(split.length >= 1) {
                int pointAfter = split[0].length() + trigger.length();
                input = split[0] + replacement + input.substring(pointAfter);
            } else input = input.replace(trigger, replacement);
        }

        return input;
    }
}
