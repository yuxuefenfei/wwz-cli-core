/**
 * 命令模型抽象。
 *
 * <p>该包包含整条 CLI 流程共享的中立数据结构和小型契约。业务项目通常用枚举实现
 * {@link com.wwz.cli.core.command.CommandSpec}，并用
 * {@link com.wwz.cli.core.command.CommandHolder} 在各层之间传递解析结果。</p>
 */
package com.wwz.cli.core.command;
