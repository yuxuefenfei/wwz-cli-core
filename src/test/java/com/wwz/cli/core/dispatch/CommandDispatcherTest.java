package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandSpec;
import com.wwz.cli.core.handler.CommandHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandDispatcherTest {

    @Test
    void shouldDispatchUnknownCommandToUnknownHandler() {
        var dispatcher = new CommandDispatcher<>(
                new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN),
                List.of(handler(List.of(TestCommand.UNKNOWN), "help")));

        var output = dispatcher.execute(new CommandHolder("missing", List.of(), Map.of()));

        assertThat(output).isEqualTo("help");
    }

    @Test
    void shouldRejectDuplicateCommandRegistration() {
        var resolver = new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN);
        var handlers = List.of(
                handler(List.of(TestCommand.HELP), "first"),
                handler(List.of(TestCommand.HELP), "second"));

        assertThatThrownBy(() -> new CommandDispatcher<>(resolver, handlers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("命令重复注册");
    }

    private static CommandHandler<TestCommand> handler(List<TestCommand> commands, String output) {
        return new CommandHandler<>() {
            @Override
            public List<TestCommand> supportedCommands() {
                return commands;
            }

            @Override
            public String handle(TestCommand command, CommandHolder commandHolder) {
                return output;
            }
        };
    }

    enum TestCommand implements CommandSpec {
        HELP("help"),
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
