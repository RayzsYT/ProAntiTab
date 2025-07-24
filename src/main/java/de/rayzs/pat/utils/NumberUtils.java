package de.rayzs.pat.utils;

public class NumberUtils {

    public static boolean isBetween(int n, int l, int r) {
        int min = Math.min(l, r);
        int max = Math.max(l, r);

        return n >= min && n <= max;
    }

    public static boolean isDigit(String str) {
        if (str == null || str.isBlank())
            return false;

        int length = str.length();

        for (int i = 0; i < length; i++) {

            char c = str.charAt(i);

            if (c == '.' || c == ',')
                continue;

            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

}
