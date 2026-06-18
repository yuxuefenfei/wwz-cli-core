package com.wwz.cli.core.parser;

import com.wwz.cli.core.command.CommandHolder;

/**
 * 将原始输入行转换为中立的命令对象。
 */
public interface CommandParser {

    /**
     * 解析来自交互式 shell 或测试用例的用户输入。
     */
    CommandHolder parse(String line);
}
