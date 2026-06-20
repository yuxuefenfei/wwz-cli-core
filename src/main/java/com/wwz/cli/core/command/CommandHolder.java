package com.wwz.cli.core.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 已解析的命令输入。
 *
 * <p>{@code CommandHolder} 是解析器、分发器、校验器和命令处理器之间传递的中立数据结构。
 * 它不会依赖任何具体业务命令枚举，因此同一套解析结果可以被不同 CLI 应用复用。</p>
 */
public class CommandHolder {

    private final String name;
    private final List<String> args;
    private final Map<String, String> options;

    /**
     * 创建不可变的命令对象。
     */
    public CommandHolder(String name, List<String> args, Map<String, String> options) {
        this.name = name;
        this.args = Collections.unmodifiableList(args);
        this.options = Collections.unmodifiableMap(options);
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    /**
     * 按索引获取位置参数，索引越界时返回 {@code null}。
     */
    public String arg(int index) {
        return arg(index, null);
    }

    /**
     * 按索引获取位置参数，索引越界时返回指定默认值。
     */
    public String arg(int index, String defaultValue) {
        if (index < 0 || index >= args.size()) {
            return defaultValue;
        }
        return args.get(index);
    }

    /**
     * 获取位置参数数量。
     */
    public int argCount() {
        return args.size();
    }

    /**
     * 获取选项值；当选项不存在时返回指定默认值。
     */
    public String option(String name, String defaultValue) {
        return options.getOrDefault(name, defaultValue);
    }

    /**
     * 判断解析结果中是否包含指定选项。
     */
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    /**
     * 获取整数类型选项值。
     */
    public int intOption(String name, int defaultValue) {
        var value = options.get(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("选项 " + name + " 需要为整数，实际值：" + value, ex);
        }
    }

    /**
     * 获取布尔类型选项值。
     */
    public boolean boolOption(String name) {
        var value = options.get(name);
        return value != null && !"false".equalsIgnoreCase(value);
    }
}
