package com.chh.ap.cs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayUtil {

    public static List<String> arrayStrToList(String str, String splitChar) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String[] split = str.split(splitChar);
        List<String> list = Arrays.asList(split);
        return list;
    }

    public static List<Long> arrayStrToLongList(String str, String splitChar) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String[] split = str.split(splitChar);
        List<Long> list = new ArrayList<Long>();
        for (int i = 0; i < split.length; i++) {
            list.add(Long.valueOf(split[i]));
        }
        return list;
    }

    public static String listToArrayStr(List<?> list, String splitChar) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; list != null && i < list.size(); i++) {
            str.append(list.get(i));
            if (i < list.size() - 1) {
                str.append(splitChar);
            }
        }
        return str.toString();
    }
}
