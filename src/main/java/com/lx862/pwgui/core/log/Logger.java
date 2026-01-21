package com.lx862.pwgui.core.log;

import com.lx862.pwgui.core.BuildMetadata;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** The logger used for the program */
public class Logger {
    private final List<LogCallback> logListeners;
    private final List<LogEntry> entries;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public Logger() {
        this.entries = new ArrayList<>();
        this.logListeners = new ArrayList<>();
    }

    public void addListener(LogCallback logListener) {
        this.logListeners.add(logListener);

        // Send historic log to listener
        for(LogEntry line : entries) {
            logListener.onLog(line, false);
        }
    }

    public void removeListener(LogCallback logListener) {
        this.logListeners.remove(logListener);
    }

    public void error(String str, Throwable t, Object... placeholders) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw)); // Only in Java (TM)
        error(str + "\n" + sw, placeholders);
    }

    public void error(String str, Object... placeholders) {
        errorRaw(BuildMetadata.INSTANCE.name, str, placeholders);
    }

    public void errorRaw(String prefix, String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.ERROR, prefix, str, placeholders);
    }

    public void warn(String str, Object... placeholders) {
        warnRaw(BuildMetadata.INSTANCE.name, str, placeholders);
    }

    public void warnRaw(String prefix, String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.WARNING, prefix, str, placeholders);
    }

    public void info(String str, Object... placeholders) {
        infoRaw(BuildMetadata.INSTANCE.name, str, placeholders);
    }

    public void infoRaw(String prefix, String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.INFO, prefix, str, placeholders);
    }

    public void debug(String str, Object... placeholders) {
        debug(BuildMetadata.INSTANCE.name, str, placeholders);
    }

    public void debug(String prefix, String str, Object... placeholders) {
        writeLog(LogEntry.LogLevel.DEBUG, prefix, str, placeholders);
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

    public interface LogCallback {
        /**
         * Invoked when a log entry is appended
         * @param entry The log entry
         * @param isRealtime Whether the log entry has just arrived, or is it historic log
         */
        void onLog(LogEntry entry, boolean isRealtime);
    }
}
