package com.wwz.cli.core.command;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandHolderTest {

    @Test
    void shouldReadArgsSafely() {
        var holder = new CommandHolder("get", List.of("a"), Map.of());

        assertThat(holder.arg(0)).isEqualTo("a");
        assertThat(holder.arg(1)).isNull();
        assertThat(holder.arg(1, "fallback")).isEqualTo("fallback");
        assertThat(holder.arg(-1, "fallback")).isEqualTo("fallback");
        assertThat(holder.argCount()).isEqualTo(1);
    }

    @Test
    void shouldReadTypedOptions() {
        var holder = new CommandHolder("get", List.of(), Map.of("limit", "20", "confirm", "true", "dry-run", "false"));

        assertThat(holder.intOption("limit", 50)).isEqualTo(20);
        assertThat(holder.intOption("missing", 50)).isEqualTo(50);
        assertThat(holder.boolOption("confirm")).isTrue();
        assertThat(holder.boolOption("dry-run")).isFalse();
        assertThat(holder.boolOption("missing")).isFalse();
    }

    @Test
    void shouldRejectInvalidIntegerOption() {
        var holder = new CommandHolder("get", List.of(), Map.of("limit", "abc"));

        assertThatThrownBy(() -> holder.intOption("limit", 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("需要为整数");
    }

    @Test
    void shouldExposeImmutableCollections() {
        var holder = new CommandHolder("get", List.of("a"), Map.of("k", "v"));

        assertThatThrownBy(() -> holder.getArgs().add("b"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> holder.getOptions().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
