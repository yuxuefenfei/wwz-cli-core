package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandSpec;

/**
 * Converts raw command names to typed application commands.
 *
 * <p>Resolvers are responsible for alias and case handling. The dispatcher only uses
 * the typed command returned by this interface.</p>
 */
public interface CommandResolver<C extends CommandSpec> {

    /**
     * Resolves a raw name such as {@code list-orgs} to a command enum value.
     */
    C resolve(String name);

    /**
     * Command used when {@link #resolve(String)} cannot match the input.
     */
    C unknownCommand();
}
