package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;
import com.wwz.cli.core.handler.CommandHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Routes parsed input to the handler that owns the resolved command.
 *
 * <p>The dispatcher is deliberately small: it only resolves a command name, finds the
 * registered handler, and delegates execution. Validation, authorization, dry-run, and
 * business logic should live in application handlers or services.</p>
 *
 * @param <C> application command enum type
 */
public class CommandDispatcher<C extends CommandSpec> implements CommandExecutor {

    private final CommandResolver<C> commandResolver;
    private final Map<C, CommandHandler<C>> handlers;

    /**
     * Builds the command-to-handler registry.
     *
     * <p>Every command may be registered by exactly one handler. Duplicate registration
     * fails fast during application startup, which is much easier to diagnose than a
     * command being routed unpredictably at runtime.</p>
     */
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

    /**
     * Resolves the holder name and executes the matched handler.
     *
     * <p>Unknown command names are routed to the resolver's configured unknown command.
     * Applications normally register that command through
     * {@link com.wwz.cli.core.handler.SystemCommandHandlerSupport}.</p>
     */
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
