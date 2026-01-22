package com.lx862.pwgui.core.data.model;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/* Represents a .gitignore-formatted rules */
public class GitIgnoreRules {
    private final String[] rules;

    public GitIgnoreRules(String content) {
        this(content.split("\n"));
    }

    public GitIgnoreRules(String[] rules) {
        this.rules = rules;
    }

    /* This adds rules on top of our existing rules, essentially overlaying on top */
    public GitIgnoreRules overlay(GitIgnoreRules gitIgnoreRules) {
        int totalSize = rules.length + gitIgnoreRules.rules.length;
        String[] newRules = new String[totalSize];
        System.arraycopy(rules, 0, newRules, 0, rules.length);
        System.arraycopy(gitIgnoreRules.rules, 0, newRules, rules.length, gitIgnoreRules.rules.length);
        return new GitIgnoreRules(newRules);
    }

    /** Whether the given path is ignored by the current rulesets */
    public boolean match(Path path) {
        for(String line : rules) {
            if(line.trim().isEmpty()) continue;
            if(line.startsWith("#")) continue; // Comment

            String finalLine = line;
            if(finalLine.startsWith("/")) {
                finalLine = finalLine.substring(1);
            } else if(finalLine.startsWith("**/")) {
                finalLine = finalLine.substring(3);
            }

            PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/" + finalLine);
            if(pathMatcher.matches(path)) return true;
        }
        return false;
    }
}
