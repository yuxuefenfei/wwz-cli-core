package com.wwz.cli.core.receiver;

import com.wwz.cli.core.command.CommandHolder;

/**
 * 原始 shell 输入和已解析命令之间的边界。
 *
 * <p>默认实现会直接委托给 {@link com.wwz.cli.core.parser.CommandParser}。如果业务应用
 * 希望在解析前增加命令审计、预处理或自定义输入规范化，可以替换这一层。</p>
 */
public interface CommandReceiver {

    /**
     * 接收原始输入行，并返回解析后的命令对象。
     */
    CommandHolder receive(String line);
}
