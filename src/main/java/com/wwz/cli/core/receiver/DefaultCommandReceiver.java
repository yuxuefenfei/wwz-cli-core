package com.wwz.cli.core.receiver;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.parser.CommandParser;

/**
 * 默认命令接收器，所有解析工作都委托给 {@link CommandParser}。
 */
public class DefaultCommandReceiver implements CommandReceiver {

    private final CommandParser commandParser;

    /**
     * 使用当前应用提供的解析器创建接收器。
     */
    public DefaultCommandReceiver(CommandParser commandParser) {
        this.commandParser = commandParser;
    }

    @Override
    public CommandHolder receive(String line) {
        return commandParser.parse(line);
    }
}
