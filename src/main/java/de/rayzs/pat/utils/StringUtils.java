package de.rayzs.pat.utils;

import de.rayzs.pat.utils.message.MessageTranslator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class StringUtils {

    public static boolean startsWithIgnoreCase(String startingWithSource, String str) {
        final int strCutLength = startingWithSource.length();
        if (strCutLength >= str.length()) {
            return false;
        }

        System.out.println("\"" + str.substring(0, strCutLength) + "\"startsWith(\"" + startingWithSource + "\")");

        return startingWithSource.equalsIgnoreCase(str.substring(0, strCutLength));
    }

    public static void centralize(List<String> lines) {
        final Map<Integer, Integer> sizes = new HashMap<>();
        final String centerVariable = "%center%";

        int biggestSize = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.startsWith(centerVariable)) {
                line = MessageTranslator.colorless(line.substring(centerVariable.length()));

                int size = line.length();
                sizes.put(i, size);
                biggestSize = Math.max(biggestSize, size);
            }
        }

        if (sizes.isEmpty()) return;

        for (int i = 0; i < lines.size(); i++) {
            final int size = sizes.getOrDefault(i, -1);

            if (size != -1) {
                final int diff = biggestSize - size;
                final int left = diff / 2;
                final int right = diff - left;

                lines.set(i,
                        " ".repeat(left) + lines.get(i).substring(centerVariable.length()) + " ".repeat(right)
                );
            }
        }
    }

    public static String hashString(final String string, final String algorithm) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            final byte[] hash = messageDigest.digest(string.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder(2 * hash.length);

            for (final byte b : hash) {
                final String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

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

    public static int countMatches(Character searching, String source) {
        int count = 0;
        for (char c : source.toCharArray()) {
            if (searching == c) count++;
        }

        return count;
    }

    public static int countMatches(final String searching, final String source) {
        char[] sourceChars = source.toCharArray();

        int matches = 0, success = 0;
        for (char character : sourceChars) {
            if (success == searching.length()) {
                matches++;
                success = 0;
            }

            if (character != searching.charAt(success)) {
                success = 0;
                continue;
            }

            success++;
        }

        return matches;
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

    public static String lowercaseFirstArgument(String str) {
        final String[] split = str.split(" ");

        if (split.length <= 1) {
            return str.toLowerCase();
        }

        split[0] = split[0].toLowerCase();
        return String.join(" ", split);
    }

    public static boolean equals(String str1, String str2, boolean caseSensitive) {
        return caseSensitive ? str1.equals(str2) : str1.equalsIgnoreCase(str2);
    }

    public static String getFirstArg(String input) {
        final int index = input.indexOf(' ');
        return index == -1 ? input : input.substring(0, index);
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
