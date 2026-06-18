package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;

import java.util.List;

/**
 * Handles one or more typed commands.
 *
 * <p>Applications usually create one handler per feature area, for example
 * {@code SystemCommandHandler}, {@code OrgCommandHandler}, or {@code PatrolCommandHandler}.
 * A handler advertises the commands it owns through {@link #supportedCommands()}.</p>
 */
public interface CommandHandler<C extends CommandSpec> {

    /**
     * Commands owned by this handler.
     */
    List<C> supportedCommands();

    /**
     * Executes the command and returns text for the shell to print.
     */
    String handle(C command, CommandHolder commandHolder);
}
