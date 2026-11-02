package com.liskovsoft.sharedutils.mylogger;

import android.content.Context;

public class Log {
    public static final String LOG_TYPE_FILE = "log_type_file";
    public static final String LOG_TYPE_SYSTEM = "log_type_system";

    private static MyLogger sLogger = new SystemLogger();

    // Recent lines kept in memory for the diagnostics report. Entries older than 24h are dropped.
    private static final int RECENT_MAX_LINES = 1500;
    private static final int RECENT_MAX_LINE_LENGTH = 400;
    private static final long RECENT_MAX_AGE_MS = 24L * 60 * 60 * 1000;
    private static final java.util.ArrayDeque<long[]> sRecentTimes = new java.util.ArrayDeque<>();
    private static final java.util.ArrayDeque<String> sRecentLines = new java.util.ArrayDeque<>();
    private static long sLastPruneMs;

    private static void record(char level, String tag, String msg) {
        if (msg == null) {
            return;
        }

        if (msg.length() > RECENT_MAX_LINE_LENGTH) {
            msg = msg.substring(0, RECENT_MAX_LINE_LENGTH) + "...";
        }

        long now = System.currentTimeMillis();

        synchronized (sRecentLines) {
            if (sRecentLines.size() >= RECENT_MAX_LINES) {
                sRecentTimes.removeFirst();
                sRecentLines.removeFirst();
            }

            sRecentTimes.addLast(new long[]{now});
            sRecentLines.addLast(level + "/" + tag + ": " + msg);

            if (now - sLastPruneMs > 60 * 60 * 1000) {
                sLastPruneMs = now;
                pruneRecent(now);
            }
        }
    }

    private static void pruneRecent(long now) {
        while (!sRecentTimes.isEmpty() && now - sRecentTimes.peekFirst()[0] > RECENT_MAX_AGE_MS) {
            sRecentTimes.removeFirst();
            sRecentLines.removeFirst();
        }
    }

    /**
     * Last log lines of the app (newest last), at most 24h old. Each line is prefixed with the time.
     */
    public static java.util.List<String> getRecent() {
        java.util.List<String> result = new java.util.ArrayList<>();
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.US);
        long now = System.currentTimeMillis();

        synchronized (sRecentLines) {
            pruneRecent(now);
            java.util.Iterator<long[]> times = sRecentTimes.iterator();
            for (String line : sRecentLines) {
                result.add(format.format(new java.util.Date(times.next()[0])) + " " + line);
            }
        }

        return result;
    }

    public static void clearRecent() {
        synchronized (sRecentLines) {
            sRecentTimes.clear();
            sRecentLines.clear();
        }
    }

    public static void d(String tag, Object msg, Object... formatArgs) {
        String text = formatMsg(msg, formatArgs);
        record('D', tag, text);
        sLogger.d(tag, text);
    }

    public static void i(String tag, Object msg, Object... formatArgs) {
        String text = formatMsg(msg, formatArgs);
        record('I', tag, text);
        sLogger.i(tag, text);
    }

    public static void w(String tag, Object msg, Object... formatArgs) {
        String text = formatMsg(msg, formatArgs);
        record('W', tag, text);
        sLogger.w(tag, text);
    }

    public static void e(String tag, Object msg, Object... formatArgs) {
        String text = formatMsg(msg, formatArgs);
        record('E', tag, text);
        sLogger.e(tag, text);
    }

    public static void i(String tag, Object msg, Throwable ex) {
        i(tag, msg + " " + ex.getMessage());
    }

    public static void e(String tag, Object msg, Throwable ex) {
        if (msg != null && ex != null) {
            e(tag, msg + " " + ex.getMessage());
        }
    }

    public static void d(String tag, Object msg, Throwable ex) {
        d(tag, msg + " " + ex.getMessage());
    }

    public static void w(String tag, Object msg, Throwable ex) {
        w(tag, msg + " " + ex.getMessage());
    }

    /**
     * In case of file, flushes all data to disk
     */
    public static void flush() {
        sLogger.flush();
    }

    public static void init(Context context, String logType, String customLabel) {
        if (sLogger.getLogType().equals(logType)) {
            return;
        }

        switch (logType) {
            case LOG_TYPE_FILE:
                sLogger = new FileLogger(context, customLabel);
                break;
            case LOG_TYPE_SYSTEM:
                sLogger = new SystemLogger();
                break;
        }
    }

    public static String getLogType() {
        if (sLogger != null) {
            return sLogger.getLogType();
        }

        return LOG_TYPE_SYSTEM;
    }

    private static String formatMsg(Object msg, Object... formatArgs) {
        String result = null;

        if (msg != null && formatArgs != null && formatArgs.length > 0) {
            result = String.format(msg.toString(), formatArgs);
        } else if (msg != null) {
            result = msg.toString();
        }

        return result;
    }
}
