package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;

import java.util.List;

public interface CommandHandler<C extends CommandSpec> {

    List<C> supportedCommands();

    String handle(C command, CommandHolder commandHolder);
}
