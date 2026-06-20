package com.wwz.cli.core.command;

import java.util.Collections;
import java.util.Map;

/**
 * 命令执行的结构化结果。
 *
 * <p>使用结构化结果后，执行层可以明确表达成功/失败、输出文本和附加元数据，
 * 避免仅靠字符串区分“无输出”和“错误信息”。</p>
 */
public class CommandResult {

    private final boolean success;
    private final String output;
    private final Map<String, Object> metadata;

    private CommandResult(boolean success, String output, Map<String, Object> metadata) {
        this.success = success;
        this.output = output == null ? "" : output;
        this.metadata = Collections.unmodifiableMap(metadata == null ? Map.of() : metadata);
    }

    public static CommandResult ok(String output) {
        return new CommandResult(true, output, Map.of());
    }

    public static CommandResult ok(String output, Map<String, Object> metadata) {
        return new CommandResult(true, output, metadata);
    }

    public static CommandResult empty() {
        return ok("");
    }

    public static CommandResult error(String output) {
        return new CommandResult(false, output, Map.of());
    }

    public static CommandResult error(String output, Map<String, Object> metadata) {
        return new CommandResult(false, output, metadata);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

    public boolean hasOutput() {
        return !output.isEmpty();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
