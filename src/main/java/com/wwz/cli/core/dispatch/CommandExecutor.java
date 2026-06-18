package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;

/**
 * Minimal execution boundary used by the interactive shell.
 *
 * <p>The shell only needs an object that can execute a parsed command and return output.
 * Keeping this as a small interface makes it easy to test shell subclasses or decorate
 * execution with logging/metrics in the application.</p>
 */
public interface CommandExecutor {

    /**
     * Executes one parsed command and returns the text to print.
     */
    String execute(CommandHolder commandHolder);
}
