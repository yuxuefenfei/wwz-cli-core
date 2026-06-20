package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SystemCommandHandlerSupportTest {

    private final SystemCommandHandlerSupport<TestCommand> handler = new SystemCommandHandlerSupport<>(
            TestCommand.EMPTY,
            TestCommand.HELP,
            TestCommand.CLEAR,
            TestCommand.UNKNOWN,
            () -> "help text");

    @Test
    void shouldReturnEmptyResultForEmptyCommand() {
        var result = handler.handle(TestCommand.EMPTY, new CommandHolder("", List.of(), Map.of()));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOutput()).isEmpty();
    }

    @Test
    void shouldDelegateHelpToSupplier() {
        assertThat(handler.handle(TestCommand.HELP, new CommandHolder("help", List.of(), Map.of())).getOutput())
                .isEqualTo("help text");
    }

    @Test
    void shouldReturnAnsiClearScreenForClearCommand() {
        assertThat(handler.handle(TestCommand.CLEAR, new CommandHolder("clear", List.of(), Map.of())).getOutput())
                .isEqualTo(SystemCommandHandlerSupport.ANSI_CLEAR_SCREEN);
    }

    @Test
    void shouldReturnUnknownErrorForUnknownCommand() {
        var result = handler.handle(TestCommand.UNKNOWN, new CommandHolder("wat", List.of(), Map.of()));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getOutput()).contains("未知命令：wat");
    }

    enum TestCommand implements CommandSpec {
        EMPTY(""),
        HELP("help"),
        CLEAR("clear"),
        UNKNOWN("__unknown__");

        private final String commandName;

        TestCommand(String commandName) {
            this.commandName = commandName;
        }

        @Override
        public String commandName() {
            return commandName;
        }
    }
}
