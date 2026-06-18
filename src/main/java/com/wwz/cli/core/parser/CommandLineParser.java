package com.wwz.cli.core.parser;

import com.wwz.cli.core.command.CommandHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandLineParser implements CommandParser {

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
