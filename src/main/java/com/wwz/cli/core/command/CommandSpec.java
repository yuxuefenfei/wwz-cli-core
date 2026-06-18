package com.wwz.cli.core.command;

import java.util.List;

/**
 * 业务应用命令枚举需要实现的契约。
 *
 * <p>核心库不内置任何业务命令。每个应用自己定义枚举并实现该接口，再交给
 * {@link com.wwz.cli.core.dispatch.EnumCommandResolver} 使用。这样既能保持业务命令的
 * 类型安全，也能复用通用解析和分发能力。</p>
 */
public interface CommandSpec {

    /**
     * 命令的标准名称，通常用于帮助文案和重复注册错误提示。
     */
    String commandName();

    /**
     * 该命令支持的所有输入名称。
     *
     * <p>默认只使用 {@link #commandName()}。当命令需要别名时可以重写该方法，
     * 例如同时支持 {@code clear} 和 {@code cls}。</p>
     */
    default List<String> aliases() {
        return List.of(commandName());
    }
}
