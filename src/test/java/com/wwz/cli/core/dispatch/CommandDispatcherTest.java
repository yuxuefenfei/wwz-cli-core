package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;
import com.wwz.cli.core.command.CommandSpec;
import com.wwz.cli.core.handler.CommandHandler;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandDispatcherTest {

    @Test
    void shouldRouteToMatchingHandler() {
        var dispatcher = new CommandDispatcher<>(
                new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN),
                List.of(handler(List.of(TestCommand.HELP), CommandResult.ok("help")),
                        handler(List.of(TestCommand.UNKNOWN), CommandResult.error("unknown"))));

        var output = dispatcher.execute(new CommandHolder("help", List.of(), Map.of()));

        assertThat(output.isSuccess()).isTrue();
        assertThat(output.getOutput()).isEqualTo("help");
    }

    @Test
    void shouldDispatchUnknownCommandToUnknownHandler() {
        var dispatcher = new CommandDispatcher<>(
                new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN),
                List.of(handler(List.of(TestCommand.UNKNOWN), CommandResult.error("unknown"))));

        var output = dispatcher.execute(new CommandHolder("missing", List.of(), Map.of()));

        assertThat(output.isSuccess()).isFalse();
        assertThat(output.getOutput()).isEqualTo("unknown");
    }

    @Test
    void shouldRejectDuplicateCommandRegistration() {
        var resolver = new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN);
        var handlers = List.of(
                handler(List.of(TestCommand.HELP), CommandResult.ok("first")),
                handler(List.of(TestCommand.HELP), CommandResult.ok("second")));

        assertThatThrownBy(() -> new CommandDispatcher<>(resolver, handlers))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("命令重复注册");
    }

    @Test
    void shouldPropagateHandlerException() {
        var dispatcher = new CommandDispatcher<>(
                new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN),
                List.of(failingHandler(List.of(TestCommand.HELP)), handler(List.of(TestCommand.UNKNOWN), CommandResult.error("unknown"))));

        assertThatThrownBy(() -> dispatcher.execute(new CommandHolder("help", List.of(), Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("boom");
    }

    private static CommandHandler<TestCommand> handler(List<TestCommand> commands, CommandResult output) {
        return new CommandHandler<>() {
            @Override
            public List<TestCommand> supportedCommands() {
                return commands;
            }

            @Override
            public CommandResult handle(TestCommand command, CommandHolder commandHolder) {
                return output;
            }
        };
    }

    private static CommandHandler<TestCommand> failingHandler(List<TestCommand> commands) {
        return new CommandHandler<>() {
            @Override
            public List<TestCommand> supportedCommands() {
                return commands;
            }

            @Override
            public CommandResult handle(TestCommand command, CommandHolder commandHolder) {
                throw new IllegalStateException("boom");
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
