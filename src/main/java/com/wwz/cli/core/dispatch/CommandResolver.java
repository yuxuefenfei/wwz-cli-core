package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandSpec;

/**
 * 将原始命令名称转换成业务应用的类型化命令。
 *
 * <p>resolver 负责处理别名和大小写规则。dispatcher 只使用该接口返回的类型化命令。</p>
 */
public interface CommandResolver<C extends CommandSpec> {

    /**
     * 将 {@code list-orgs} 这类原始名称解析为命令枚举值。
     */
    C resolve(String name);

    /**
     * 当 {@link #resolve(String)} 无法匹配输入时使用的命令。
     */
    C unknownCommand();
}
