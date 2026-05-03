package com.hlgs.lettergen.util;

import java.io.Closeable;
import java.io.IOException;

public final class FileUtil {

    private FileUtil() {
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // no-op
        }
    }

    public static String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}

// Made with Bob
