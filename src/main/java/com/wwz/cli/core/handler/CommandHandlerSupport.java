package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandOperation;
import com.wwz.cli.core.command.CommandResult;
import com.wwz.cli.core.command.CommandSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 命令 handler 的便捷基类。
 */
public abstract class CommandHandlerSupport<C extends CommandSpec> implements CommandHandler<C> {

    private final Map<C, CommandOperation<C>> operations = new LinkedHashMap<>();

    @Override
    public List<C> supportedCommands() {
        return List.copyOf(operations.keySet());
    }

    @Override
    public CommandResult handle(C command, CommandHolder commandHolder) {
        var operation = operations.get(command);
        if (operation == null) {
            throw new IllegalArgumentException("命令处理器不支持：" + command.commandName());
        }
        return operation.execute(command, commandHolder);
    }

    /**
     * 注册一个命令操作。
     */
    protected void register(C command, CommandOperation<C> operation) {
        var previous = operations.put(command, operation);
        if (previous != null) {
            throw new IllegalStateException("命令重复注册：" + command.commandName());
        }
    }
}
