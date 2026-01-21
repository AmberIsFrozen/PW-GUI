package com.lx862.pwgui.core;

import com.lx862.pwgui.PWGUI;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/** The logger used for the program */
public class Logger {
    private final List<LogCallback> logListeners;
    private final List<String> lines;

    public Logger() {
        this.lines = new ArrayList<>();
        this.logListeners = new ArrayList<>();
    }

    public void addListener(LogCallback logListener) {
        this.logListeners.add(logListener);

        // Send historic log to listener
        for(String line : lines) {
            logListener.onLog(line, false);
        }
    }

    public void removeListener(LogCallback logListener) {
        this.logListeners.remove(logListener);
    }

    public void error(String str) {
        error("[" + BuildMetadata.INSTANCE.name + "]", str);
    }

    public void error(String prefix, String str) {
        log(prefix + " [ERROR]", str, System.err);
    }

    public void warn(String str) {
        warn("[" + BuildMetadata.INSTANCE.name + "]", str);
    }

    public void warn(String prefix, String str) {
        log(prefix + " [WARN]", str, System.out);
    }

    public void exception(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw)); // Only in Java (TM)
        error(sw.toString());
    }

    public void info(String str) {
        info("[" + BuildMetadata.INSTANCE.name + "]", str);
    }

    public void info(String prefix, String str) {
        log(prefix + " [INFO]", str, System.out);
    }

    public void debug(String str) {
        debug("[" + BuildMetadata.INSTANCE.name + "]", str);
    }

    public void debug(String prefix, String str) {
        if(PWGUI.getConfig().debugMode.getValue()) log(prefix + " [DEBUG]", str, System.out);
    }

    private void log(String prefix, String str, PrintStream printStream) {
        String finalMessage = prefix + " " + str;
        lines.add(finalMessage);
        printStream.println(finalMessage);
        for(LogCallback logListener : logListeners) {
            logListener.onLog(finalMessage, true);
        }
    }

    public String[] getLogHistory() {
        return this.lines.toArray(String[]::new);
    }

    public interface LogCallback {
        void onLog(String content, boolean isRealtime);
    }
}
