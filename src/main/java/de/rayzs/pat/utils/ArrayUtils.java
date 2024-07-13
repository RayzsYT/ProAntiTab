package de.rayzs.pat.utils;

import java.util.*;

public class ArrayUtils {

    public static boolean compareStringArrays(List<String> listA, List<String> listB) {
        if(listA == null || listB == null) return false;

        boolean found = Arrays.equals(listA.toArray(), listB.toArray());
        if(found) return true;

        for (String s : listA) {
            found = listB.contains(s);
            if(!found) break;
        }

        if(found) {

            for (String s : listB) {
                found = listA.contains(s);
                if(!found) break;
            }

        }

        return found;
    }
}
