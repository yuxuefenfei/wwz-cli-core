package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;
import com.wwz.cli.core.handler.CommandHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandDispatcher<C extends CommandSpec> implements CommandExecutor {

    private final CommandResolver<C> commandResolver;
    private final Map<C, CommandHandler<C>> handlers;

    public CommandDispatcher(CommandResolver<C> commandResolver, List<? extends CommandHandler<C>> commandHandlers) {
        this.commandResolver = commandResolver;
        this.handlers = new LinkedHashMap<>();
        for (CommandHandler<C> handler : commandHandlers) {
            for (C command : handler.supportedCommands()) {
                var previous = handlers.put(command, handler);
                if (previous != null) {
                    throw new IllegalStateException("命令重复注册：" + command.commandName());
                }
            }
        }
    }

    @Override
    public String execute(CommandHolder commandHolder) {
        var command = commandResolver.resolve(commandHolder.getName());
        var handler = handlers.get(command);
        if (handler == null) {
            handler = handlers.get(commandResolver.unknownCommand());
        }
        if (handler == null) {
            throw new IllegalStateException("缺少未知命令处理器");
        }
        return handler.handle(command, commandHolder);
    }
}
