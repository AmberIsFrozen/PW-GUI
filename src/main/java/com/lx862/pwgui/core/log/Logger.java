package com.lx862.pwgui.core.log;

import com.lx862.pwgui.core.Config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/** The logger used for the program */
public class Logger {
    private final List<LogEntry> entries;
    private final String contextName;
    private static final List<LogCallback> logListeners = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private static final List<Logger> registeredLoggers = new ArrayList<>();

    static {
        appendStdoutLogger();
    }

    public Logger(String contextName) {
        this.entries = new ArrayList<>();
        this.contextName = contextName;
        registeredLoggers.add(this);
    }

    public static void addListener(LogCallback logListener) {
        logListeners.add(logListener);

        List<LogEntry> entries = new ArrayList<>();
        for(Logger logger : registeredLoggers) {
            entries.addAll(Arrays.stream(logger.getLogHistory()).toList());
        }
        entries.sort((e, f) -> Math.toIntExact(e.timeMs() - f.timeMs()));

        // Send historic log to listener
        for(LogEntry line : entries) {
            logListener.onLog(line, false);
        }
    }

    public static void removeListener(LogCallback logListener) {
        logListeners.remove(logListener);
    }

    public void error(String str, Throwable t, Object... placeholders) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw)); // Only in Java (TM)
        error(str + "\n" + sw, placeholders);
    }

    public void error(String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.ERROR, contextName, str, placeholders);
    }

    public void warn(String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.WARNING, contextName, str, placeholders);
    }

    public void info(String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.INFO, contextName, str, placeholders);
    }

    public void debug(String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.DEBUG, contextName, str, placeholders);
    }

    private void writeLog(LogEntry.LogLevel logLevel, String prefix, String str, Object... placeholders) {
        String logMessage = str;
        for(Object o : placeholders) {
            logMessage = logMessage.replaceFirst("\\{}", String.valueOf(o));
        }

        long logTimeMillis = System.currentTimeMillis();

        String timePrefix = sdf.format(new Date(logTimeMillis));
        String finalMessage = String.format("[%s] [%s/%s]: %s", timePrefix, prefix, logLevel.prefix(), logMessage);

        LogEntry entry = new LogEntry(logLevel, finalMessage, System.currentTimeMillis());
        entries.add(entry);
        for(LogCallback logListener : logListeners) {
            logListener.onLog(entry, true);
        }
    }

    public LogEntry[] getLogHistory() {
        return this.entries.toArray(LogEntry[]::new);
    }

    private static void appendStdoutLogger() {
        addListener(((entry, isRealtime) -> {
            if(entry.logLevel() == LogEntry.LogLevel.ERROR) {
                System.err.println(entry.message());
            } else if(entry.logLevel() != LogEntry.LogLevel.DEBUG || Config.getInstance().debugMode.value()) {
                System.out.println(entry.message());
            }
        }));
    }

    public interface LogCallback {
        /**
         * Invoked when a log entry is appended
         * @param entry The log entry
         * @param isRealtime Whether the log entry has just arrived, or is it historic log
         */
        void onLog(LogEntry entry, boolean isRealtime);
    }
}
