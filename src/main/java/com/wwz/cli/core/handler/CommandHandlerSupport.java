package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandOperation;
import com.wwz.cli.core.command.CommandSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 命令 handler 的便捷基类。
 *
 * <p>子类通常在构造函数中注册命令操作：</p>
 *
 * <pre>{@code
 * public OrgCommandHandler(OrgService service) {
 *     register(Command.ORGS, this::listOrgs);
 * }
 * }</pre>
 *
 * <p>这种模式让命令注册位置靠近具体实现，也能在 handler 内部及时发现重复注册问题。</p>
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
     * 注册一个命令操作。
     *
     * <p>建议只在子类构造函数中调用。若同一个 handler 内重复注册同一个命令，
     * 会被视为编程错误并立即失败。</p>
     */
    protected void register(C command, CommandOperation<C> operation) {
        var previous = operations.put(command, operation);
        if (previous != null) {
            throw new IllegalStateException("命令重复注册：" + command.commandName());
        }
    }
}
