package com.wwz.cli.core.command;

import java.util.List;

public interface CommandSpec {

    String commandName();

    default List<String> aliases() {
        return List.of(commandName());
    }
}
