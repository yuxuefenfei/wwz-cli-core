package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;

/**
 * 交互式 shell 使用的最小执行边界。
 */
public interface CommandExecutor {

    /**
     * 执行一条已解析命令，并返回结构化结果。
     */
    CommandResult execute(CommandHolder commandHolder);
}
