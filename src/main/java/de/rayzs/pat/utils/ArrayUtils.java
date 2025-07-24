package de.rayzs.pat.utils;

import java.util.*;

public class ArrayUtils {

    public static boolean containsIgnoreCase(List<String> list, String element) {
        return list.stream().anyMatch(element::equalsIgnoreCase);
    }

    public static boolean compareStringArrays(List<String> listA, List<String> listB) {
        if(listA == null || listB == null)
            return false;

        if (listA.size() != listB.size())
            return false;

        if (Arrays.equals(listA.toArray(), listB.toArray()))
            return true;

        Collections.sort(listA);
        Collections.sort(listB);

        final int length = listA.size();
        for (int i = 0; i < length; i++) {
            if (!listA.get(i).equalsIgnoreCase(listB.get(i)))
                return false;
        }

        return true;
    }
}
