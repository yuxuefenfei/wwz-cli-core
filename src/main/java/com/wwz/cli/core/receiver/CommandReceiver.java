package com.wwz.cli.core.receiver;

import com.wwz.cli.core.command.CommandHolder;

/**
 * Boundary between raw shell input and parsed commands.
 *
 * <p>The default implementation simply delegates to {@link com.wwz.cli.core.parser.CommandParser}.
 * Applications may replace this layer if they want command auditing, preprocessing, or
 * custom input normalization before parsing.</p>
 */
public interface CommandReceiver {

    /**
     * Receives a raw input line and returns the parsed command holder.
     */
    CommandHolder receive(String line);
}
