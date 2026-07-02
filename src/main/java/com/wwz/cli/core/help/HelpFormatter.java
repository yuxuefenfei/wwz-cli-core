package com.wwz.cli.core.help;

import org.jline.utils.WCWidth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帮助文本格式化工具。
 */
public class HelpFormatter {

    static final int DEFAULT_WIDTH = 100;
    private static final int MIN_WIDTH = 40;
    private static final int COMMAND_INDENT = 2;
    private static final int DESCRIPTION_INDENT = 4;
    private static final int DESCRIPTION_COLUMN = 32;

    private HelpFormatter() {
    }

    /**
     * 按分组格式化命令帮助文本，默认将每行限制在 100 个终端显示列内。
     */
    public static String format(List<CommandHelpEntry> entries) {
        return format(entries, DEFAULT_WIDTH);
    }

    /**
     * 按分组格式化命令帮助文本。
     *
     * <p>短命令与说明紧凑地显示在同一行；命令较长时，说明移到下一行。
     * 命令和说明都会按终端显示宽度换行，中文字符按两个显示列计算。</p>
     *
     * @param entries  帮助条目
     * @param maxWidth 每行最大终端显示列数，最小为 40
     */
    public static String format(List<CommandHelpEntry> entries, int maxWidth) {
        if (entries.isEmpty()) {
            return "";
        }
        if (maxWidth < MIN_WIDTH) {
            throw new IllegalArgumentException("maxWidth must be at least " + MIN_WIDTH);
        }

        var lineSeparator = System.lineSeparator();
        var grouped = entries.stream().collect(Collectors.groupingBy(
                CommandHelpEntry::getGroup,
                java.util.LinkedHashMap::new,
                Collectors.toList()));
        var output = new StringBuilder();
        grouped.forEach((group, groupEntries) -> {
            if (output.length() > 0) {
                output.append(lineSeparator);
            }
            output.append(group).append(':').append(lineSeparator);
            for (CommandHelpEntry entry : groupEntries) {
                appendEntry(output, entry, maxWidth, lineSeparator);
            }
        });
        return output.toString().stripTrailing();
    }

    private static void appendEntry(StringBuilder output, CommandHelpEntry entry,
                                    int maxWidth, String lineSeparator) {
        var command = commandColumn(entry);
        var inlineCommandWidth = DESCRIPTION_COLUMN - COMMAND_INDENT - 2;
        if (displayWidth(command) <= inlineCommandWidth) {
            var prefix = spaces(COMMAND_INDENT) + command;
            output.append(prefix)
                    .append(spaces(DESCRIPTION_COLUMN - displayWidth(prefix)));
            appendInlineDescription(output, entry.getDescription(), maxWidth, lineSeparator);
            return;
        }

        appendWrapped(output, command, COMMAND_INDENT, maxWidth, lineSeparator);
        appendWrapped(output, entry.getDescription(), DESCRIPTION_INDENT,
                maxWidth, lineSeparator);
    }

    private static void appendInlineDescription(StringBuilder output, String description,
                                                int maxWidth, String lineSeparator) {
        var lines = wrap(description, maxWidth - DESCRIPTION_COLUMN);
        if (lines.isEmpty()) {
            output.append(lineSeparator);
            return;
        }
        output.append(lines.get(0)).append(lineSeparator);
        for (int i = 1; i < lines.size(); i++) {
            output.append(spaces(DESCRIPTION_COLUMN))
                    .append(lines.get(i))
                    .append(lineSeparator);
        }
    }

    private static void appendWrapped(StringBuilder output, String text, int indent,
                                      int maxWidth, String lineSeparator) {
        var lines = wrap(text, maxWidth - indent);
        if (lines.isEmpty()) {
            output.append(lineSeparator);
            return;
        }
        var prefix = spaces(indent);
        for (String line : lines) {
            output.append(prefix).append(line).append(lineSeparator);
        }
    }

    private static List<String> wrap(String text, int width) {
        var lines = new ArrayList<String>();
        if (text == null || text.isBlank()) {
            return lines;
        }

        var current = new StringBuilder();
        for (String word : text.trim().split("\\s+")) {
            if (current.length() > 0 && displayWidth(current.toString()) + 1 + displayWidth(word) <= width) {
                current.append(' ').append(word);
                continue;
            }
            if (current.length() > 0) {
                lines.add(current.toString());
                current.setLength(0);
            }
            var parts = splitToWidth(word, width);
            for (int i = 0; i < parts.size() - 1; i++) {
                lines.add(parts.get(i));
            }
            current.append(parts.get(parts.size() - 1));
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    private static List<String> splitToWidth(String text, int width) {
        var parts = new ArrayList<String>();
        var part = new StringBuilder();
        var partWidth = 0;
        for (int offset = 0; offset < text.length();) {
            var codePoint = text.codePointAt(offset);
            var codePointWidth = codePointWidth(codePoint);
            if (part.length() > 0 && partWidth + codePointWidth > width) {
                parts.add(part.toString());
                part.setLength(0);
                partWidth = 0;
            }
            part.appendCodePoint(codePoint);
            partWidth += codePointWidth;
            offset += Character.charCount(codePoint);
        }
        if (part.length() > 0) {
            parts.add(part.toString());
        }
        return parts;
    }

    private static String commandColumn(CommandHelpEntry entry) {
        var aliases = entry.getAliases().isEmpty() ? "" : " | " + String.join(" | ", entry.getAliases());
        var usage = entry.getUsage().isBlank() ? "" : " " + entry.getUsage();
        return entry.getName() + aliases + usage;
    }

    private static int displayWidth(String text) {
        var width = 0;
        for (int offset = 0; offset < text.length();) {
            var codePoint = text.codePointAt(offset);
            width += codePointWidth(codePoint);
            offset += Character.charCount(codePoint);
        }
        return width;
    }

    private static int codePointWidth(int codePoint) {
        return Math.max(0, WCWidth.wcwidth(codePoint));
    }

    private static String spaces(int count) {
        return " ".repeat(Math.max(0, count));
    }
}
