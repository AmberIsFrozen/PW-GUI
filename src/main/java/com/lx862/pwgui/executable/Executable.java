package com.lx862.pwgui.executable;

import com.lx862.pwgui.core.log.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Executable {
    private ExecutorService executor;
    private Path workingDirectory;
    private final Logger logger;
    protected final List<String> keywords;
    protected final List<String> potentialPaths;
    protected final String programName;
    protected String executableLocation;

    public Executable(Logger logger, String programName) {
        if(logger == null) throw new IllegalArgumentException("Logger must not be null!");

        this.workingDirectory = Paths.get(System.getProperty("user.dir"));
        this.executor = Executors.newSingleThreadExecutor();
        this.keywords = new ArrayList<>();
        this.potentialPaths = new ArrayList<>();
        this.programName = programName;
        this.executableLocation = null;
        this.logger = logger;
    }

    public boolean updateExecutableLocation(String executableOverride) {
        String probedLocation = probe(executableOverride);
        this.executableLocation = probedLocation;

        return probedLocation != null;
    }

    public String probe(String executableOverride) {
        if(executableOverride != null) {
            if(isOurIntendedProgram(executableOverride)) {
                logger.info("{} executable is specified at {}", programName, executableOverride);
                return executableOverride;
            } else {
                logger.info("{} executable specified at {} is not valid!", programName, executableOverride);
            }
        }

        if(executableLocation == null) {
            logger.info("Probing for {} executable...", programName);
            for(String potentialPath : potentialPaths) {
                if(isOurIntendedProgram(potentialPath)) {
                    logger.info("Found {} executable at {}", programName, potentialPath);
                    return potentialPath;
                }
            }
        }

        logger.info("Cannot probe {} executable!", programName);
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
        logger.info("Working directory changed to {}", newPath.toString());
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

    private void ensureExecutorActive() {
        if(executor == null || executor.isShutdown()) executor = Executors.newSingleThreadExecutor();
    }

    public class ProgramArgumentBuilder {
        protected final List<String> args;

        public ProgramArgumentBuilder(String... existingArgs) {
            this.args = new ArrayList<>(List.of(existingArgs));
        }

        public ProgramArgumentBuilder append(String... strs) {
            args.addAll(Arrays.asList(strs));
            return this;
        }

        public ProgramArgumentBuilder append(List<String> strs) {
            args.addAll(strs);
            return this;
        }

        public ProgramExecution build() {
            ensureExecutorActive();
            args.add(0, executableLocation);
            ProcessBuilder processBuilder = new ProcessBuilder(args.toArray(new String[0]));
            processBuilder.directory(workingDirectory.toFile());

            return new ProgramExecution(logger, programName, processBuilder, executor);
        }
    }
}
