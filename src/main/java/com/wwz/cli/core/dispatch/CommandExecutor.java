package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;

/**
 * 交互式 shell 使用的最小执行边界。
 *
 * <p>shell 只需要一个能够执行已解析命令并返回输出文本的对象。将它抽成小接口后，
 * 业务项目可以更容易地测试 shell 子类，也可以在执行入口外层增加日志、指标等装饰逻辑。</p>
 */
public interface CommandExecutor {

    /**
     * 执行一条已解析命令，并返回需要打印的文本。
     */
    String execute(CommandHolder commandHolder);
}
