package com.wwz.cli.core.command;

import java.util.List;

/**
 * Contract implemented by an application's command enum.
 *
 * <p>The core library does not ship business commands. Instead, each application
 * defines an enum, implements this interface, and passes it to
 * {@link com.wwz.cli.core.dispatch.EnumCommandResolver}. This gives the application
 * type-safe commands while keeping the parser/dispatcher reusable.</p>
 */
public interface CommandSpec {

    /**
     * Canonical command name shown in help text and duplicate-registration errors.
     */
    String commandName();

    /**
     * All accepted names for this command.
     *
     * <p>The default uses only {@link #commandName()}. Override this method when the
     * command should support aliases, for example {@code clear} and {@code cls}.</p>
     */
    default List<String> aliases() {
        return List.of(commandName());
    }
}
