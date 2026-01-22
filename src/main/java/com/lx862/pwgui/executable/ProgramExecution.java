package com.lx862.pwgui.executable;

import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.log.Logger;
import com.lx862.pwgui.util.Util;

import java.io.*;
import java.util.concurrent.ExecutorService;

public class ProgramExecution extends Task {
    private final ProcessBuilder processBuilder;
    private Process process;

    public ProgramExecution(Logger logger, String taskName, ProcessBuilder processBuilder, ExecutorService defaultExecutor) {
        super(taskName, defaultExecutor);
        this.processBuilder = processBuilder;

        onOutput((stdout) -> { // Display log when we got a new line
            logger.info(stdout.content());
        });
    }

    @Override
    public void run(String reason, ExecutorService executor) {
        PWGUI.LOGGER.info("Running command \"{}\" due to \"{}\"", String.join(" ", processBuilder.command()), reason);

        executor.submit(() -> {
            try {
                this.process = this.processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    int c;
                    while ((c = reader.read()) != -1) {
                        if(c == '\n' || c == '\r') { // Newline character
                            String line = sb.toString();
                            callOutputListeners(new OutputMessage(line, false));
                            sb = new StringBuilder(); // Clear current line
                        } else {
                            sb.append((char)c);
                            String line = sb.toString();
                            if(line.endsWith("[Y/n]: ")) { // Inline prompt
                                callOutputListeners(new OutputMessage(line, true));
                            }
                        }
                    }
                }

                this.process.waitFor();
                int exitValue = this.process.exitValue();
                callExitListeners(exitValue);
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                callOutputListeners(new OutputMessage(Util.withBracketPrefix(String.format("Failed to execute %s:\n%s", getTaskName(), e.getMessage())), false));
                callExitListeners(-2);
            } catch (InterruptedException ignored) {
            }
        });
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
