package com.wwz.cli.core.help;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HelpFormatterTest {

    @Test
    void shouldFormatShortEntriesInCompactColumns() {
        var output = HelpFormatter.format(List.of(
                CommandHelpEntry.builder("help")
                        .group("系统")
                        .description("查看帮助")
                        .build(),
                CommandHelpEntry.builder("orgs")
                        .group("业务")
                        .aliases(List.of("list-orgs"))
                        .usage("[--limit 50]")
                        .description("查询机构")
                        .build()));

        assertThat(output)
                .contains("系统:")
                .contains("  help                          查看帮助")
                .contains("业务:")
                .contains("  orgs | list-orgs [--limit 50]")
                .contains("    查询机构");
    }

    @Test
    void shouldWrapLongCommandsAndChineseDescriptionsWithinConfiguredWidth() {
        var output = HelpFormatter.format(List.of(
                CommandHelpEntry.builder("patrol-check")
                        .group("巡检命令")
                        .usage("report --org-id <编号> --org-file <yml> --plan-id <ID> --time <时间>")
                        .description("按机构编号或名称筛选，支持生成较长的巡检结果说明")
                        .build()), 48);

        assertThat(output).contains("巡检命令:", "\n    按机构编号");
        assertThat(output.lines())
                .allSatisfy(line -> assertThat(displayWidth(line)).isLessThanOrEqualTo(48));
    }

    @Test
    void shouldRejectUnusableOutputWidth() {
        assertThatThrownBy(() -> HelpFormatter.format(List.of(
                CommandHelpEntry.builder("help").build()), 39))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("40");
    }

    @Test
    void shouldReturnEmptyStringForEmptyEntries() {
        assertThat(HelpFormatter.format(List.of())).isEmpty();
    }

    private static int displayWidth(String text) {
        return text.codePoints()
                .map(codePoint -> codePoint >= 0x2e80 ? 2 : 1)
                .sum();
    }
}
