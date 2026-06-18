package com.wwz.cli.core.shell;

public class InteractiveShellOptions {

    private final String prompt;
    private final String historyFileName;
    private final String startupMessage;

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
