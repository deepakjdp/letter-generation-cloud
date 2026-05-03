package com.hlgs.lettergen.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter FILE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {
    }

    public static String nowForFile() {
        return LocalDateTime.now().format(FILE_FORMAT);
    }

    public static String nowForDisplay() {
        return LocalDateTime.now().format(DISPLAY_FORMAT);
    }
}

// Made with Bob
