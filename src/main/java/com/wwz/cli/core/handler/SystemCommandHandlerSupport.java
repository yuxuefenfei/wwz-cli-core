package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;

import java.util.function.Supplier;

/**
 * 通用交互式 shell 命令的可复用 handler。
 *
 * <p>业务应用仍然拥有自己的命令枚举，因此该类要求应用传入空输入、帮助、清屏和未知命令
 * 对应的枚举值。帮助文本通过 supplier 延迟获取，方便业务项目复用自己的帮助文案生成器。</p>
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

    private String empty(C command, CommandHolder commandHolder) {
        return "";
    }

    private String help(C command, CommandHolder commandHolder) {
        return helpSupplier.get();
    }

    private String clear(C command, CommandHolder commandHolder) {
        return ANSI_CLEAR_SCREEN;
    }

    private String unknown(C command, CommandHolder commandHolder) {
        return "未知命令：" + commandHolder.getName() + "，输入 help 查看支持的命令";
    }
}
