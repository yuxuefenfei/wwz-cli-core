package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;

import java.util.function.Supplier;

public class SystemCommandHandlerSupport<C extends CommandSpec> extends CommandHandlerSupport<C> {

    public static final String ANSI_CLEAR_SCREEN = "\033[2J\033[3J\033[H";

    private final Supplier<String> helpSupplier;

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
