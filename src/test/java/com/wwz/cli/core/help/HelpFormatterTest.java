package com.wwz.cli.core.help;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelpFormatterTest {

    @Test
    void shouldFormatGroupedHelpEntries() {
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
                .contains("系统：")
                .contains("help")
                .contains("业务：")
                .contains("orgs | list-orgs [--limit 50]")
                .contains("查询机构");
    }

    @Test
    void shouldReturnEmptyStringForEmptyEntries() {
        assertThat(HelpFormatter.format(List.of())).isEmpty();
    }
}
