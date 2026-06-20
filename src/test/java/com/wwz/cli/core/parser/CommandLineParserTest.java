package com.wwz.cli.core.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandLineParserTest {

    private final CommandLineParser parser = new CommandLineParser();

    @Test
    void shouldReturnEmptyCommandForNullInput() {
        var command = parser.parse(null);

        assertThat(command.getName()).isEmpty();
        assertThat(command.getArgs()).isEmpty();
        assertThat(command.getOptions()).isEmpty();
    }

    @Test
    void shouldReturnEmptyCommandForBlankInput() {
        var command = parser.parse("   ");

        assertThat(command.getName()).isEmpty();
    }

    @Test
    void shouldTreatFirstTokenAsLowercaseName() {
        var command = parser.parse("GET key");

        assertThat(command.getName()).isEqualTo("get");
        assertThat(command.getArgs()).containsExactly("key");
    }

    @Test
    void shouldCollectPositionalArgs() {
        var command = parser.parse("copy source target");

        assertThat(command.getArgs()).containsExactly("source", "target");
    }

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
    void shouldParseOptionWithSpaceSeparator() {
        var command = parser.parse("orgs --keyword 投资 --limit 20");

        assertThat(command.getOptions())
                .containsEntry("keyword", "投资")
                .containsEntry("limit", "20");
    }

    @Test
    void shouldHandleSingleQuotedValue() {
        var command = parser.parse("clean --reason 'INC 001'");

        assertThat(command.getOptions()).containsEntry("reason", "INC 001");
    }

    @Test
    void shouldHandleBackslashEscape() {
        var command = parser.parse("get key\\ with\\ space");

        assertThat(command.getArgs()).containsExactly("key with space");
    }

    @Test
    void shouldKeepTrailingBackslashAsLiteral() {
        var command = parser.parse("get user\\");

        assertThat(command.getArgs()).containsExactly("user\\");
    }

    @Test
    void shouldKeepEmptyQuotedOptionValue() {
        var command = parser.parse("clean --reason \"\"");

        assertThat(command.getOptions()).containsEntry("reason", "");
    }

    @Test
    void shouldTreatDoubleDashAsArgument() {
        var command = parser.parse("run --");

        assertThat(command.getArgs()).containsExactly("--");
    }

    @Test
    void shouldRejectUnclosedDoubleQuote() {
        assertThatThrownBy(() -> parser.parse("get \"abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未闭合的引号");
    }

    @Test
    void shouldRejectUnclosedSingleQuote() {
        assertThatThrownBy(() -> parser.parse("get 'abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未闭合的引号");
    }
}
