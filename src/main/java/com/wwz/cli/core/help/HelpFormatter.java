package com.wwz.cli.core.help;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 帮助文本格式化工具。
 */
public class HelpFormatter {

    private HelpFormatter() {
    }

    /**
     * 按分组格式化命令帮助文本。
     */
    public static String format(List<CommandHelpEntry> entries) {
        if (entries.isEmpty()) {
            return "";
        }
        var lineSeparator = System.lineSeparator();
        var commandColumnWidth = entries.stream()
                .map(HelpFormatter::commandColumn)
                .mapToInt(String::length)
                .max()
                .orElse(0);
        var grouped = entries.stream().collect(Collectors.groupingBy(
                CommandHelpEntry::getGroup,
                java.util.LinkedHashMap::new,
                Collectors.toList()));
        var output = new StringBuilder();
        grouped.forEach((group, groupEntries) -> {
            if (output.length() > 0) {
                output.append(lineSeparator);
            }
            output.append(group).append('：').append(lineSeparator);
            for (CommandHelpEntry entry : groupEntries) {
                output.append("  ")
                        .append(padRight(commandColumn(entry), commandColumnWidth))
                        .append("  ")
                        .append(entry.getDescription())
                        .append(lineSeparator);
            }
        });
        return output.toString().stripTrailing();
    }

    private static String commandColumn(CommandHelpEntry entry) {
        var aliases = entry.getAliases().isEmpty() ? "" : " | " + String.join(" | ", entry.getAliases());
        var usage = entry.getUsage().isBlank() ? "" : " " + entry.getUsage();
        return entry.getName() + aliases + usage;
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }
}
