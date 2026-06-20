package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;

import java.util.List;

/**
 * 支持拦截器链的命令执行器装饰器。
 */
public class InterceptingCommandExecutor implements CommandExecutor {

    private final CommandExecutor delegate;
    private final List<CommandInterceptor> interceptors;

    public InterceptingCommandExecutor(CommandExecutor delegate, List<CommandInterceptor> interceptors) {
        this.delegate = delegate;
        this.interceptors = List.copyOf(interceptors);
    }

    @Override
    public CommandResult execute(CommandHolder commandHolder) {
        var current = commandHolder;
        for (CommandInterceptor interceptor : interceptors) {
            current = interceptor.beforeExecute(current);
        }
        try {
            var result = delegate.execute(current);
            for (CommandInterceptor interceptor : interceptors) {
                result = interceptor.afterExecute(current, result);
            }
            return result;
        } catch (RuntimeException ex) {
            var failure = ex;
            for (CommandInterceptor interceptor : interceptors) {
                try {
                    return interceptor.onError(current, failure);
                } catch (RuntimeException next) {
                    failure = next;
                }
            }
            throw failure;
        }
    }
}
