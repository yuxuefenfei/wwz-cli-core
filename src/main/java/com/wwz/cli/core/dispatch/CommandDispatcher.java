package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;
import com.wwz.cli.core.handler.CommandHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将解析后的命令输入路由到对应的命令处理器。
 *
 * <p>分发器刻意保持轻量：它只负责解析命令名、查找已注册的 handler，并把执行动作委托出去。
 * 参数校验、权限控制、预览模式和业务逻辑应放在业务项目自己的 handler 或 service 中。</p>
 *
 * @param <C> 业务应用的命令枚举类型
 */
public class CommandDispatcher<C extends CommandSpec> implements CommandExecutor {

    private final CommandResolver<C> commandResolver;
    private final Map<C, CommandHandler<C>> handlers;

    /**
     * 构建命令到 handler 的注册表。
     *
     * <p>每个命令只能由一个 handler 注册。重复注册会在应用启动阶段快速失败，
     * 这比运行时出现不可预测的路由行为更容易定位。</p>
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
     * 解析命令名称，并执行匹配到的 handler。
     *
     * <p>未知命令会被路由到 resolver 配置的 unknown 命令。业务应用通常通过
     * {@link com.wwz.cli.core.handler.SystemCommandHandlerSupport} 注册该命令的处理逻辑。</p>
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
