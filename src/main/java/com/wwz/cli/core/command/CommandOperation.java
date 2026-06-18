package com.wwz.cli.core.command;

/**
 * Function executed for one concrete command inside a handler.
 *
 * <p>{@link com.wwz.cli.core.handler.CommandHandlerSupport} stores a map from command
 * enum value to {@code CommandOperation}. This keeps handler classes compact: a handler
 * can register several related commands and implement each one as a small method.</p>
 */
@FunctionalInterface
public interface CommandOperation<C extends CommandSpec> {

    /**
     * Executes the operation and returns text that should be printed by the shell.
     */
    String execute(C command, CommandHolder commandHolder);
}
