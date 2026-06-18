package com.wwz.cli.core.parser;

import com.wwz.cli.core.command.CommandHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 类 shell 风格的命令行解析器。
 *
 * <p>该解析器支持大多数交互式运维工具需要的输入形式：</p>
 *
 * <ul>
 *     <li>quoted values: {@code --time "2026-06-17 09:00"}</li>
 *     <li>escape characters inside tokens</li>
 *     <li>long options as {@code --name=value} or {@code --name value}</li>
 *     <li>boolean switches such as {@code --confirm}, represented as {@code true}</li>
 * </ul>
 *
 * <p>第一个 token 始终作为命令名。其他非选项 token 会进入位置参数列表。
 * 选项会去掉前缀 {@code --} 后存储。</p>
 */
public class CommandLineParser implements CommandParser {

    /**
     * 将一行用户输入解析为 {@link CommandHolder}。
     */
    @Override
    public CommandHolder parse(String line) {
        var tokens = tokenize(line == null ? "" : line);
        if (tokens.isEmpty()) {
            return new CommandHolder("", List.of(), Map.of());
        }
        var name = tokens.get(0).toLowerCase(Locale.ROOT);
        var args = new ArrayList<String>();
        var options = new LinkedHashMap<String, String>();
        for (int i = 1; i < tokens.size(); i++) {
            var token = tokens.get(i);
            if (!token.startsWith("--") || token.length() == 2) {
                args.add(token);
                continue;
            }
            var option = token.substring(2);
            var equals = option.indexOf('=');
            if (equals >= 0) {
                options.put(option.substring(0, equals), option.substring(equals + 1));
                continue;
            }
            if (i + 1 < tokens.size() && !tokens.get(i + 1).startsWith("--")) {
                options.put(option, tokens.get(++i));
            } else {
                options.put(option, "true");
            }
        }
        return new CommandHolder(name, args, options);
    }

    /**
     * 将原始输入行拆分为 token，同时处理引号和反斜杠转义。
     *
     * <p>解析器会保留空引号值，因此 {@code --reason ""} 会被表示为空字符串，
     * 而不是被直接丢弃。</p>
     */
    private List<String> tokenize(String line) {
        var tokens = new ArrayList<String>();
        var current = new StringBuilder();
        var quoted = false;
        var quote = (char) 0;
        var escaped = false;
        var tokenStarted = false;
        for (int i = 0; i < line.length(); i++) {
            var ch = line.charAt(i);
            if (escaped) {
                current.append(ch);
                escaped = false;
                tokenStarted = true;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (quoted) {
                if (ch == quote) {
                    quoted = false;
                } else {
                    current.append(ch);
                }
                continue;
            }
            if (ch == '\'' || ch == '"') {
                quoted = true;
                quote = ch;
                tokenStarted = true;
                continue;
            }
            if (Character.isWhitespace(ch)) {
                if (tokenStarted) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    tokenStarted = false;
                }
                continue;
            }
            current.append(ch);
            tokenStarted = true;
        }
        if (escaped) {
            current.append('\\');
            tokenStarted = true;
        }
        if (quoted) {
            throw new IllegalArgumentException("命令存在未闭合的引号");
        }
        if (tokenStarted) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
