package com.wwz.cli.core.parser;

import com.wwz.cli.core.command.CommandHolder;

public interface CommandParser {

    CommandHolder parse(String line);
}
