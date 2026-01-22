package com.lx862.pwgui.support.git.executable;

import com.lx862.pwgui.core.log.Logger;
import com.lx862.pwgui.executable.Executable;

public class GitExecutable extends Executable {
    public static final Logger LOGGER = new Logger("GitExec");
    public static final GitExecutable INSTANCE = new GitExecutable(LOGGER);

    public GitExecutable(Logger logger) {
        super(logger, "Git");
        keywords.add("These are common Git commands used in various situations:");
        keywords.add("See 'git help git' for an overview of the system.");

        potentialPaths.add("git"); // Added in PATH
        potentialPaths.add("C:/Program Files (x86)/Git/bin/git.exe");
        potentialPaths.add("C:/Program Files (x86)/Git/libexec/git-core/git.exe");
    }
}
