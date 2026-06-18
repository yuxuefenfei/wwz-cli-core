package com.wwz.cli.core.shell;

/**
 * Display and history settings for {@link InteractiveShellRunner}.
 */
public class InteractiveShellOptions {

    private final String prompt;
    private final String historyFileName;
    private final String startupMessage;

    /**
     * @param prompt prompt shown before every input line, for example {@code data-kit> }
     * @param historyFileName file name stored under the current user's home directory
     * @param startupMessage message printed once before the shell loop starts
     */
    public InteractiveShellOptions(String prompt, String historyFileName, String startupMessage) {
        this.prompt = prompt;
        this.historyFileName = historyFileName;
        this.startupMessage = startupMessage;
    }

    public String prompt() {
        return prompt;
    }

    public String historyFileName() {
        return historyFileName;
    }

    public String startupMessage() {
        return startupMessage;
    }
}
