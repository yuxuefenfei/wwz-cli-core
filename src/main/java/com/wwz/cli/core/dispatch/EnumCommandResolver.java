package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandSpec;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 基于枚举命令集的默认 resolver。
 */
public class EnumCommandResolver<C extends Enum<C> & CommandSpec> implements CommandResolver<C> {

    private final Map<String, C> lookup = new HashMap<>();
    private final C unknownCommand;

    /**
     * 为枚举命令类型创建 resolver。
     */
    public EnumCommandResolver(Class<C> enumType, C unknownCommand) {
        this.unknownCommand = unknownCommand;
        for (C command : enumType.getEnumConstants()) {
            for (String alias : command.aliases()) {
                var normalizedAlias = alias.toLowerCase(Locale.ROOT);
                var previous = lookup.put(normalizedAlias, command);
                if (previous != null && previous != command) {
                    throw new IllegalStateException("命令别名冲突：" + alias
                            + " 同时映射到 " + previous.commandName()
                            + " 和 " + command.commandName());
                }
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
