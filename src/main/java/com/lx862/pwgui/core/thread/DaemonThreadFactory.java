package com.lx862.pwgui.core.thread;

/* Creates a daemon thread, allowing the JVM to shut-down */
public class DaemonThreadFactory extends NamedThreadFactory {
    public DaemonThreadFactory(String name) {
        super(name);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = super.newThread(r);
        thread.setDaemon(true);
        return thread;
    }
}
