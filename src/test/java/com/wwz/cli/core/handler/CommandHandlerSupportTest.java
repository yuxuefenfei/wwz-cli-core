package com.wwz.cli.core.handler;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;
import com.wwz.cli.core.command.CommandSpec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandHandlerSupportTest {

    @Test
    void shouldReturnSupportedCommandsAndExecuteOperation() {
        var handler = new TestHandler();

        assertThat(handler.supportedCommands()).containsExactly(TestCommand.HELP);
        assertThat(handler.handle(TestCommand.HELP, new CommandHolder("help", List.of(), Map.of())).getOutput())
                .isEqualTo("help");
    }

    @Test
    void shouldRejectDuplicateRegistration() {
        assertThatThrownBy(DuplicateHandler::new)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("命令重复注册");
    }

    @Test
    void shouldThrowForUnregisteredCommand() {
        var handler = new TestHandler();

        assertThatThrownBy(() -> handler.handle(TestCommand.UNKNOWN, new CommandHolder("missing", List.of(), Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("命令处理器不支持");
    }

    static class TestHandler extends CommandHandlerSupport<TestCommand> {
        TestHandler() {
            register(TestCommand.HELP, (command, holder) -> CommandResult.ok("help"));
        }
    }

    static class DuplicateHandler extends CommandHandlerSupport<TestCommand> {
        DuplicateHandler() {
            register(TestCommand.HELP, (command, holder) -> CommandResult.ok("first"));
            register(TestCommand.HELP, (command, holder) -> CommandResult.ok("second"));
        }
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
