package com.wwz.cli.core.parser;

import com.wwz.cli.core.command.CommandHolder;

/**
 * Converts a raw input line into a neutral command holder.
 */
public interface CommandParser {

    /**
     * Parses user input from an interactive shell or test.
     */
    CommandHolder parse(String line);
}
