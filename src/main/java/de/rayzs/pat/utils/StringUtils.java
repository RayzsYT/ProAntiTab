package de.rayzs.pat.utils;

import java.util.*;

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

    public static String replaceTriggers(String input, String replacement, String... triggers) {
        for (String trigger : triggers) {
            if(!input.contains(trigger)) continue;
            input = input.replace(trigger, replacement);
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

    public static String getLineText(List<String> lines, int line) {
        if(line >= lines.size() || line < 1) return null;
        return lines.get(line);
    }

    public static int countLetters(String input, char trigger, boolean breakup) {
        int count = 0;
        for (char c : input.toCharArray()) {
            if(c != trigger && breakup) break;
            count++;
        }

        return count;
    }

    public static String remove(String input, String... targets) {
        return replaceTriggers(input, "", targets);
    }

    public static String replace(String input, String... replacements) {
        final HashMap<String, String> REPLACEMENTS = new HashMap<>();

        if(replacements != null) {
            String firstReplacementInput = null, secondReplacementInput = null;
            for (String replacement : replacements) {
                if (firstReplacementInput == null) firstReplacementInput = replacement;
                else secondReplacementInput = replacement;

                if (firstReplacementInput != null && secondReplacementInput != null) {
                    REPLACEMENTS.put(firstReplacementInput, secondReplacementInput);
                    firstReplacementInput = null;
                    secondReplacementInput = null;
                }
            }
        }

        String text = input;
        if(replacements != null)
            for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet())
                text = text.replace(entry.getKey(), entry.getValue());

        return text;
    }

    public static String buildSortedStringList(List<String> list, String splitter, String format, String placeholder, boolean reversed) {
        Collections.sort(list);
        if(reversed) Collections.reverse(list);
        return buildStringList(list, splitter, format, placeholder);
    }

    public static String buildStringList(List<String> list, String splitter, String format, String placeholder) {
        StringBuilder builder = new StringBuilder();
        boolean end;

        for (int i = 0; i < list.size(); i++) {
            end = i >= list.size() - 1;
            builder.append(format.replace(placeholder, list.get(i)));
            if (!end && splitter != null) builder.append(splitter);
        }

        return builder.toString();
    }

    public static String buildStringList(List<String> list) {
        StringBuilder builder = new StringBuilder();
        boolean end;

        for (int i = 0; i < list.size(); i++) {
            end = i >= list.size() - 1;
            builder.append(list.get(i).replace("&", "§"));
            if (!end) builder.append("\n");
        }

        return builder.toString();
    }

    public static String buildStringListWithoutColors(List<String> list) {
        StringBuilder builder = new StringBuilder();
        boolean end;

        for (int i = 0; i < list.size(); i++) {
            end = i >= list.size() - 1;
            builder.append(list.get(i));
            if (!end) builder.append("\n");
        }

        return builder.toString();
    }
}
