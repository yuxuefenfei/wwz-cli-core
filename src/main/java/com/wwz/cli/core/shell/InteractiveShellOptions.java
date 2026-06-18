package com.wwz.cli.core.shell;

/**
 * {@link InteractiveShellRunner} 使用的显示和历史记录配置。
 */
public class InteractiveShellOptions {

    private final String prompt;
    private final String historyFileName;
    private final String startupMessage;

    /**
     * @param prompt 每行输入前展示的提示符，例如 {@code demo> }
     * @param historyFileName 存放在当前用户 home 目录下的历史记录文件名
     * @param startupMessage shell 循环启动前打印一次的启动提示
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
