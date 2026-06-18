package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandSpec;

public interface CommandResolver<C extends CommandSpec> {

    C resolve(String name);

    C unknownCommand();
}
