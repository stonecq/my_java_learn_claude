package com.agent.core.utils;

public class StringUtils {
    public static String truncate(String content, int maxLength) {
        if (content == null) {
            return "null";
        }
        if (content.length() > maxLength) {
            return content.substring(0, maxLength) + "...[truncated]";
        }
        return content;
    }
}
