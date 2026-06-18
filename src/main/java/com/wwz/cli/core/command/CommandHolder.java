package com.wwz.cli.core.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 已解析的命令输入。
 *
 * <p>{@code CommandHolder} 是解析器、分发器、校验器和命令处理器之间传递的中立数据结构。
 * 它不会依赖任何具体业务命令枚举，因此同一套解析结果可以被不同 CLI 应用复用。</p>
 *
 * <p>例如命令行 {@code clean target --org-id=1002 --confirm} 会被解析成：</p>
 *
 * <ul>
 *     <li>{@code name}: {@code clean}</li>
 *     <li>{@code args}: {@code ["target"]}</li>
 *     <li>{@code options}: {@code {"org-id": "1002", "confirm": "true"}}</li>
 * </ul>
 */
public class CommandHolder {

    private final String name;
    private final List<String> args;
    private final Map<String, String> options;

    /**
     * 创建不可变的命令对象。
     *
     * <p>传入的列表和映射会被包装成不可修改集合，避免命令在校验和执行过程中被意外改写。</p>
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
     * 获取选项值；当选项不存在时返回指定默认值。
     */
    public String option(String name, String defaultValue) {
        return options.getOrDefault(name, defaultValue);
    }

    /**
     * 判断解析结果中是否包含指定选项。
     *
     * <p>布尔开关会被表示为 {@code optionName=true}，因此调用方可以用该方法判断
     * {@code --confirm} 这类开关是否被传入。</p>
     */
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }
}
