/**
 * 命令行解析。
 *
 * <p>解析器负责把原始文本转换为 {@link com.wwz.cli.core.command.CommandHolder}。
 * 默认解析器刻意保持小而类 shell 化，覆盖引号字符串、转义字符、位置参数、长选项和布尔开关。</p>
 */
package com.wwz.cli.core.parser;
