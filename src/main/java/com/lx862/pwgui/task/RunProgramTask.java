package com.lx862.pwgui.task;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.log.Logger;
import com.lx862.pwgui.util.Util;

import java.io.*;
import java.util.concurrent.ExecutorService;

public class RunProgramTask extends Task {
    private final ProcessBuilder processBuilder;
    private Process process;

    public RunProgramTask(Logger logger, String taskName, ProcessBuilder processBuilder, ExecutorService defaultExecutor) {
        super(taskName, defaultExecutor);
        this.processBuilder = processBuilder;

        onOutput((stdout) -> { // Display log when we got a new line
            logger.info(stdout.content());
        });
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        PWGUI.LOGGER.info("Running command \"{}\" due to \"{}\"", getCommand(), reason);

        executor.submit(() -> {
            try {
                this.process = this.processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    int c;
                    while ((c = reader.read()) != -1) {
                        if(c == '\n' || c == '\r') { // Newline character
                            String line = sb.toString();
                            submitOutput(new OutputMessage(line, false));
                            sb = new StringBuilder(); // Clear current line
                        } else {
                            sb.append((char)c);
                            String line = sb.toString();
                            if(line.endsWith("[Y/n]: ")) { // Inline prompt
                                submitOutput(new OutputMessage(line, true));
                            }
                        }
                    }
                }

                this.process.waitFor();
                int exitValue = this.process.exitValue();
                submitExitResult(ExitResult.code(exitValue));
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                submitOutput(new OutputMessage(Util.withBracketPrefix(String.format("Failed to execute %s:\n%s", getTaskName(), e.getMessage())), false));
                submitExitResult(ExitResult.code(-2));
            } catch (InterruptedException e) {
                submitExitResult(ExitResult.terminated(e));
            }
        });
    }

    public String getCommand() {
        return String.join(" ", processBuilder.command());
    }

    @Override
    public void terminate() {
        if(this.process != null && this.process.isAlive()) this.process.destroy();
    }

    public void enterInput(String input) {
        if(this.process != null) {
            PrintWriter pw = new PrintWriter(process.getOutputStream());
            pw.write(input + "\n");
            pw.flush();
            PWGUI.LOGGER.info("Input {} to {}", input, getTaskName());
        }
    }
}
