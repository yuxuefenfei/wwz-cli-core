package com.wwz.cli.core.command;

@FunctionalInterface
public interface CommandOperation<C extends CommandSpec> {

    String execute(C command, CommandHolder commandHolder);
}
