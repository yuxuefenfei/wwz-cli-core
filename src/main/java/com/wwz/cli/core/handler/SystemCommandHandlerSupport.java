package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;
import com.wwz.cli.core.command.CommandSpec;

import java.util.function.Supplier;

/**
 * 通用交互式 shell 命令的可复用 handler。
 */
public class SystemCommandHandlerSupport<C extends CommandSpec> extends CommandHandlerSupport<C> {

    /**
     * 常见终端用于清屏、清空滚动缓冲区并把光标移动到左上角的 ANSI 序列。
     */
    public static final String ANSI_CLEAR_SCREEN = "\033[2J\033[3J\033[H";

    private final Supplier<String> helpSupplier;

    /**
     * 注册内置系统命令操作。
     */
    public SystemCommandHandlerSupport(C emptyCommand,
                                       C helpCommand,
                                       C clearCommand,
                                       C unknownCommand,
                                       Supplier<String> helpSupplier) {
        this.helpSupplier = helpSupplier;
        register(emptyCommand, this::empty);
        register(helpCommand, this::help);
        register(clearCommand, this::clear);
        register(unknownCommand, this::unknown);
    }

    private CommandResult empty(C command, CommandHolder commandHolder) {
        return CommandResult.empty();
    }

    private CommandResult help(C command, CommandHolder commandHolder) {
        return CommandResult.ok(helpSupplier.get());
    }

    private CommandResult clear(C command, CommandHolder commandHolder) {
        return CommandResult.ok(ANSI_CLEAR_SCREEN);
    }

    private CommandResult unknown(C command, CommandHolder commandHolder) {
        return CommandResult.error("未知命令：" + commandHolder.getName() + "，输入 help 查看支持的命令");
    }
}
