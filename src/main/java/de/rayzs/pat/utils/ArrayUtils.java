package de.rayzs.pat.utils;

import java.util.*;

public class ArrayUtils {

    /**
     * Searches for the element inside the collection.
     * If the element looking for is of type String, it will check
     * for an element inside the collection which is similar to the element
     * ignore the letter casing.
     *
     * @return Returns True if found. Returns False otherwise.
     */
    public static <T> boolean containsIgnoreCase(final Collection<T> collection, final T element) {
            if (element instanceof String str) {
                return collection.stream().anyMatch(a -> str.equalsIgnoreCase(a.toString()));
            }

            return collection.contains(element);
    }

    /**
     * Checks and sees if both collections are the same.
     * @return Returns True is both collections are the same. Returns False otherwise.
     */
    public static <T> boolean isSame(final Collection<T> a, final Collection<T> b) {

        if(a == null || b == null)
            return false;

        if (a.size() != b.size())
            return false;

        if (Arrays.equals(a.toArray(), b.toArray()))
            return true;

        return isSubset(a, b) && isSubset(b, a);
    }

    /**
     * If collection A is a subset of collection B.
     * @return Returns True if collection A is a subset of B. Returns False otherwise.
     */
    public static <T> boolean isSubset(final Collection<T> a, final Collection<T> b) {

        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        if (a.isEmpty()) {
            return true;
        }

        for (T t : a) {
            if (!b.contains(t)) {
                return false;
            }
        }

        return true;
    }
}
