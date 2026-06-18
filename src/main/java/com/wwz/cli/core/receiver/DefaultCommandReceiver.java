package com.wwz.cli.core.receiver;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.parser.CommandParser;

public class DefaultCommandReceiver implements CommandReceiver {

    private final CommandParser commandParser;

    public DefaultCommandReceiver(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    @Override
    public CommandHolder receive(String line) {
        return commandParser.parse(line);
    }
}
