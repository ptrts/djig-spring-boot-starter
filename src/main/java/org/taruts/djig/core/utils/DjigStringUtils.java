package org.taruts.djig.core.utils;

public class DjigStringUtils {

    public static String ensureEndsWithSlash(String refreshPath) {
        return ensureEndsWith(refreshPath, '/');
    }

    public static String ensureEndsWith(String refreshPath, char c) {
        if (refreshPath.charAt(refreshPath.length() - 1) != c) {
            refreshPath = refreshPath + c;
        }
        return refreshPath;
    }
}
