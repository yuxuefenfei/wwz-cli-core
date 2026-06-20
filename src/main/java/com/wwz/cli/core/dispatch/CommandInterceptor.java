package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;

/**
 * 命令执行拦截器。
 */
public interface CommandInterceptor {

    /**
     * 命令执行前调用，可返回原对象，也可返回替换后的命令对象。
     */
    default CommandHolder beforeExecute(CommandHolder holder) {
        return holder;
    }

    /**
     * 命令成功执行后调用，可返回原结果，也可返回加工后的结果。
     */
    default CommandResult afterExecute(CommandHolder holder, CommandResult result) {
        return result;
    }

    /**
     * 命令执行异常时调用。默认重新抛出异常。
     */
    default CommandResult onError(CommandHolder holder, RuntimeException ex) {
        throw ex;
    }
}
