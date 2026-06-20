package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;
import com.wwz.cli.core.command.CommandSpec;

import java.util.List;

/**
 * 处理一个或多个类型化命令。
 */
public interface CommandHandler<C extends CommandSpec> {

    /**
     * 当前 handler 负责的命令集合。
     */
    List<C> supportedCommands();

    /**
     * 执行命令，并返回结构化执行结果。
     */
    CommandResult handle(C command, CommandHolder commandHolder);
}
