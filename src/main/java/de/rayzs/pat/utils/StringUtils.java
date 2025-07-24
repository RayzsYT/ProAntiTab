package de.rayzs.pat.utils;

import java.util.*;

public class StringUtils {

    public static String replaceFirst(String input, String trigger, String replacement) {

        if (!input.contains(trigger)) {
            return input;
        }

        String[] split = input.split(trigger.equals("*") ? "\\*" : trigger);

        if (split.length >= 1) {
            int pointAfter = split[0].length() + trigger.length();
            input = split[0] + replacement + input.substring(pointAfter);
        } else input = input.replace(trigger, replacement);

        return input;
    }

    public static String replaceLast(String input, String trigger, String replacement) {
        if (!input.contains(trigger)) {
            return input;
        }

        String[] split = input.split(trigger.equals("*") ? "\\*" : trigger);
        if (split.length >= 1) {
            StringBuilder result = new StringBuilder();
            String current;

            int i;
            for (i = 0; i < split.length; i++) {
                current = split[i];
                result.append(current);

                if (i < split.length -1)
                    result.append(" ");
            }

            input = result.toString();

        } else input = input.replace(trigger, replacement);

        return input;
    }

    public static String replaceElementsFromString(String input, List<String> targets, String replacement) {
        final String[] args = input.split(" ");
        final StringBuilder result = new StringBuilder();
        final int max = args.length;

        String part;
        int i;

        for (i = 0; i < max; i++) {
            part = args[i];

            if (ArrayUtils.containsIgnoreCase(targets, part))
                part = replacement;

            result.append(part);
            if (i < args.length -1)
                result.append(" ");
        }

        return result.toString();
    }

    public static String replaceTriggers(String input, String replacement, String... triggers) {
        for (String trigger : triggers) {
            if (!input.contains(trigger))
                continue;

            input = input.replace(trigger, replacement);
        }

        return input;
    }

    public static String replace(String input, String... replacements) {
        final HashMap<String, String> REPLACEMENTS = new HashMap<>();

        if (replacements.length > 0) {
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
        if (replacements.length > 0)
            for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet())
                text = text.replace(entry.getKey(), entry.getValue());

        return text;
    }

    public static String remove(String input, String... targets) {
        return replaceTriggers(input, "", targets);
    }

    public static int countMatches(Character character, String string) {
        int count = 0;
        for (char c : string.toCharArray()) {
            if (character == c) count++;
        }

        return count;
    }

    public static boolean isLowercased(String str) {
        if (str == null || str.isBlank())
            return false;

        int length = str.length();

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);

            if (!Character.isAlphabetic(c))
                continue;

            if (!Character.isLowerCase(str.charAt(i)))
                return false;
        }

        return true;
    }

    public static boolean isUppercased(String str) {
        if (str == null || str.isBlank())
            return false;

        int length = str.length();

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);

            if (!Character.isAlphabetic(c))
                continue;

            if (!Character.isUpperCase(c))
                return false;
        }

        return true;
    }

    public static String getFirstArg(String input) {
        if (input.contains(" ")) {
            String[] split = input.split(" ");

            if (split.length > 0)
                input = split[0];
        }

        return input;
    }

    public static String getLineText(List<String> lines, int line) {
        if (line >= lines.size() || line < 1)
            return null;

        return lines.get(line);
    }

    public static int countLetters(String input, char trigger, boolean breakup) {
        if (input == null || input.isEmpty())
            return -1;

        int count = 0;
        for (char c : input.toCharArray()) {
            if (c != trigger && breakup)
                break;

            count++;
        }

        return count;
    }

    public static String getStringList(List<String> list, String splitter) {
        return getStringList(list, splitter, false, false);
    }

    public static String getSortedStringList(List<String> list, String splitter) {
        return getStringList(list, splitter, true, false);
    }

    public static String getReversedStringList(List<String> list, String splitter) {
        return getStringList(list, splitter, true, true);
    }

    private static String getStringList(List<String> list, String splitter, boolean sorted, boolean reversed) {
        if (sorted)
            Collections.sort(list);

        if (reversed)
            Collections.reverse(list);

        return String.join(splitter, list);
    }
}
