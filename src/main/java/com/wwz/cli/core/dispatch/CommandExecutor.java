package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;

public interface CommandExecutor {

    String execute(CommandHolder commandHolder);
}
