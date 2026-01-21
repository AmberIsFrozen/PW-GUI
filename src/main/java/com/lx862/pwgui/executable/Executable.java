package com.lx862.pwgui.executable;

import com.lx862.pwgui.PWGUI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Executable {
    private ExecutorService executor;
    private Path workingDirectory;
    protected final List<String> keywords;
    protected final List<String> potentialPaths;
    protected final String programName;
    protected String executableLocation;

    public Executable(String programName) {
        this.workingDirectory = Paths.get(System.getProperty("user.dir"));
        this.executor = Executors.newSingleThreadExecutor();
        this.keywords = new ArrayList<>();
        this.potentialPaths = new ArrayList<>();
        this.programName = programName;
        this.executableLocation = null;
    }

    public boolean updateExecutableLocation(String executableOverride) {
        String probedLocation = probe(executableOverride);
        this.executableLocation = probedLocation;

        return probedLocation != null;
    }

    public String probe(String executableOverride) {
        if(executableOverride != null) {
            if(isOurIntendedProgram(executableOverride)) {
                PWGUI.LOGGER.info("{} executable is specified at {}", programName, executableOverride);
                return executableOverride;
            } else {
                PWGUI.LOGGER.info("{} executable specified at {} is not valid!", programName, executableOverride);
            }
        }

        if(executableLocation == null) {
            PWGUI.LOGGER.info("Probing for {} executable...", programName);
            for(String potentialPath : potentialPaths) {
                if(isOurIntendedProgram(potentialPath)) {
                    PWGUI.LOGGER.info("Found {} executable at {}", programName, potentialPath);
                    return potentialPath;
                }
            }
        }

        PWGUI.LOGGER.info("Cannot probe {} executable!", programName);
        return null;
    }

    public boolean usable() {
        return executableLocation != null;
    }

    protected boolean isOurIntendedProgram(String location) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(location);
            Process process = processBuilder.start();

            int expectedKeywordsHit = keywords.size();
            int actualKeywordsHit = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if(keywords.contains(line)) actualKeywordsHit++;
                }
            }

            return expectedKeywordsHit == actualKeywordsHit;
        } catch (Exception e) {
            return false;
        }
    }

    public void changeWorkingDirectory(Path newPath) {
        PWGUI.LOGGER.info("Working directory for {} changed to {}", programName, newPath.toString());
        this.workingDirectory = newPath;
    }

    public ProgramArgumentBuilder buildCommand(String... args) {
        return new ProgramArgumentBuilder(args);
    }

    public String getProgramName() {
        return programName;
    }

    public void dispose() {
        executor.shutdownNow();
    }

    public class ProgramArgumentBuilder {
        protected final List<String> args;

        public ProgramArgumentBuilder(String... existingArgs) {
            args = new ArrayList<>(List.of(existingArgs));
        }

        public ProgramArgumentBuilder append(String... strs) {
            for(String str : strs) {
                args.add(str);
            }
            return this;
        }

        public ProgramArgumentBuilder append(List<String> strs) {
            args.addAll(strs);
            return this;
        }

        public ProgramExecution build() {
            args.add(0, executableLocation);
            ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(new String[0]));
            processBuilder.directory(workingDirectory.toFile());

            if(executor == null || executor.isShutdown()) executor = Executors.newSingleThreadExecutor();

            return new ProgramExecution(programName, processBuilder, executor);
        }
    }
}
