package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandOperation;
import com.wwz.cli.core.command.CommandSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience base class for command handlers.
 *
 * <p>Subclasses register command operations in their constructor:</p>
 *
 * <pre>{@code
 * public OrgCommandHandler(OrgService service) {
 *     register(Command.ORGS, this::listOrgs);
 * }
 * }</pre>
 *
 * <p>This pattern keeps registration close to implementation and gives duplicate
 * registration checks inside the handler itself.</p>
 */
public abstract class CommandHandlerSupport<C extends CommandSpec> implements CommandHandler<C> {

    private final Map<C, CommandOperation<C>> operations = new LinkedHashMap<>();

    @Override
    public List<C> supportedCommands() {
        return List.copyOf(operations.keySet());
    }

    @Override
    public String handle(C command, CommandHolder commandHolder) {
        var operation = operations.get(command);
        if (operation == null) {
            throw new IllegalArgumentException("命令处理器不支持：" + command.commandName());
        }
        return operation.execute(command, commandHolder);
    }

    /**
     * Registers one command operation.
     *
     * <p>Use this from subclass constructors. Registering the same command twice inside
     * one handler is treated as a programming error and fails immediately.</p>
     */
    protected void register(C command, CommandOperation<C> operation) {
        var previous = operations.put(command, operation);
        if (previous != null) {
            throw new IllegalStateException("命令重复注册：" + command.commandName());
        }
    }
}
