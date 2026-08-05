package de.rayzs.pat.utils;

public class NumberUtils {

    private static final char[] EXCEPTIONS = {'.', ',', 'k', 'm', 'b'};

    public static boolean isBetween(int n, int l, int r) {
        final int min = Math.min(l, r),
                  max = Math.max(l, r);

        return min <= n && n <= max;
    }

    public static boolean isDigit(String str) {
        if (str == null || str.isBlank())
            return false;

        int length = str.length();

        for (int i = 0; i < length; i++) {
            char c = str.charAt(i);

            if (isExceptionalDigit(c))
                continue;

            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    private static boolean isExceptionalDigit(char c) {
        for (char q : EXCEPTIONS) {
            if (c == q) {
                return true;
            }

            if (Character.isAlphabetic(c) && c == Character.toUpperCase(q)) {
                return true;
            }
        }

        return false;
    }
}
