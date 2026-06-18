package com.wwz.cli.core.receiver;

import com.wwz.cli.core.command.CommandHolder;

public interface CommandReceiver {

    CommandHolder receive(String line);
}
