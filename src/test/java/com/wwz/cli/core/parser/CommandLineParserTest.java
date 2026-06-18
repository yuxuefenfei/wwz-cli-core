package com.wwz.cli.core.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandLineParserTest {

    private final CommandLineParser parser = new CommandLineParser();

    @Test
    void shouldParseOptionsArgsQuotesAndBooleanSwitch() {
        var command = parser.parse("repair target --org-id=1002 --time \"2026-06-17 09:00\" --confirm");

        assertThat(command.getName()).isEqualTo("repair");
        assertThat(command.getArgs()).containsExactly("target");
        assertThat(command.getOptions())
                .containsEntry("org-id", "1002")
                .containsEntry("time", "2026-06-17 09:00")
                .containsEntry("confirm", "true");
    }

    @Test
    void shouldKeepEmptyQuotedOptionValue() {
        var command = parser.parse("clean --reason \"\"");

        assertThat(command.getOptions()).containsEntry("reason", "");
    }

    @Test
    void shouldRejectUnclosedQuote() {
        assertThatThrownBy(() -> parser.parse("get \"abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未闭合的引号");
    }
}
