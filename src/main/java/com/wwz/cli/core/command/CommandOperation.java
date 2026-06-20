package com.wwz.cli.core.command;

/**
 * 单个具体命令对应的执行函数。
 *
 * <p>{@link com.wwz.cli.core.handler.CommandHandlerSupport} 会维护命令枚举值到
 * {@code CommandOperation} 的映射。这样 handler 可以注册多个相关命令，并把每个命令
 * 实现成一个小方法，类结构会更紧凑。</p>
 */
@FunctionalInterface
public interface CommandOperation<C extends CommandSpec> {

    /**
     * 执行命令操作，并返回结构化执行结果。
     */
    CommandResult execute(C command, CommandHolder commandHolder);
}
