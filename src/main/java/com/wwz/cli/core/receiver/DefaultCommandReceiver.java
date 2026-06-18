package com.wwz.cli.core.receiver;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.parser.CommandParser;

/**
 * Default receiver that delegates all parsing to a {@link CommandParser}.
 */
public class DefaultCommandReceiver implements CommandReceiver {

    private final CommandParser commandParser;

    /**
     * Creates a receiver with the parser used by the current application.
     */
    public DefaultCommandReceiver(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    @Override
    public CommandHolder receive(String line) {
        return commandParser.parse(line);
    }
}
