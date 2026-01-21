package com.lx862.pwgui.core.log;

public record LogEntry(LogLevel logLevel, String message, long timeMs) {
    public enum LogLevel {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARNING("WARN"),
        ERROR("ERROR");

        private final String prefix;

        LogLevel(String prefix) {
            this.prefix = prefix;
        }

        public String prefix() {
            return this.prefix;
        }
    }
}
