package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandSpec;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnumCommandResolver<C extends Enum<C> & CommandSpec> implements CommandResolver<C> {

    private final Map<String, C> lookup = new HashMap<>();
    private final C unknownCommand;

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
