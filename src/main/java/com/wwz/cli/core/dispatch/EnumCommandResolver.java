package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandSpec;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 基于枚举命令集的默认 resolver。
 *
 * <p>每个枚举值都需要实现 {@link CommandSpec}。构造时会把所有别名按小写建立索引，
 * 因此命令查找不区分大小写，并且时间复杂度为 O(1)。</p>
 */
public class EnumCommandResolver<C extends Enum<C> & CommandSpec> implements CommandResolver<C> {

    private final Map<String, C> lookup = new HashMap<>();
    private final C unknownCommand;

    /**
     * 为枚举命令类型创建 resolver。
     *
     * @param enumType 命令枚举类型
     * @param unknownCommand 未知或空白命令名称对应的枚举值
     */
    public EnumCommandResolver(Class<C> enumType, C unknownCommand) {
        this.unknownCommand = unknownCommand;
        for (C command : enumType.getEnumConstants()) {
            for (String alias : command.aliases()) {
                lookup.put(alias.toLowerCase(Locale.ROOT), command);
            }
        }
    }

    @Override
    public C resolve(String name) {
        if (name == null) {
            return unknownCommand;
        }
        return lookup.getOrDefault(name.trim().toLowerCase(Locale.ROOT), unknownCommand);
    }

    @Override
    public C unknownCommand() {
        return unknownCommand;
    }
}
