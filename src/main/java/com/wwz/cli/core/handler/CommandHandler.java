package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;

import java.util.List;

/**
 * 处理一个或多个类型化命令。
 *
 * <p>业务应用通常按功能域创建 handler，例如 {@code SystemCommandHandler}、
 * {@code OrgCommandHandler} 或 {@code PatrolCommandHandler}。handler 通过
 * {@link #supportedCommands()} 声明自己负责哪些命令。</p>
 */
public interface CommandHandler<C extends CommandSpec> {

    /**
     * 当前 handler 负责的命令集合。
     */
    List<C> supportedCommands();

    /**
     * 执行命令，并返回交互式 shell 需要打印的文本。
     */
    String handle(C command, CommandHolder commandHolder);
}
