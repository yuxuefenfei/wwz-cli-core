package com.wwz.cli.core.dispatch;

import com.wwz.cli.core.command.CommandSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumCommandResolverTest {

    @Test
    void shouldResolveByNameAliasAndCaseInsensitively() {
        var resolver = new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN);

        assertThat(resolver.resolve("help")).isEqualTo(TestCommand.HELP);
        assertThat(resolver.resolve("h")).isEqualTo(TestCommand.HELP);
        assertThat(resolver.resolve(" HELP ")).isEqualTo(TestCommand.HELP);
        assertThat(resolver.resolve("LIST")).isEqualTo(TestCommand.LIST);
    }

    @Test
    void shouldReturnUnknownForNullOrUnrecognizedName() {
        var resolver = new EnumCommandResolver<>(TestCommand.class, TestCommand.UNKNOWN);

        assertThat(resolver.resolve(null)).isEqualTo(TestCommand.UNKNOWN);
        assertThat(resolver.resolve("missing")).isEqualTo(TestCommand.UNKNOWN);
    }

    @Test
    void shouldRejectDuplicateAliasAcrossCommands() {
        assertThatThrownBy(() -> new EnumCommandResolver<>(ConflictCommand.class, ConflictCommand.UNKNOWN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("命令别名冲突");
    }

    enum TestCommand implements CommandSpec {
        HELP("help", "h"),
        LIST("list"),
        UNKNOWN("__unknown__");

        private final String commandName;
        private final List<String> aliases;

        TestCommand(String commandName, String... aliases) {
            this.commandName = commandName;
            this.aliases = List.of(aliases);
        }

        @Override
        public String commandName() {
            return commandName;
        }

        @Override
        public List<String> aliases() {
            var names = new java.util.ArrayList<String>();
            names.add(commandName);
            names.addAll(aliases);
            return names;
        }
    }

    enum ConflictCommand implements CommandSpec {
        A("a", "same"),
        B("b", "same"),
        UNKNOWN("__unknown__");

        private final String commandName;
        private final String alias;

        ConflictCommand(String commandName) {
            this(commandName, commandName);
        }

        ConflictCommand(String commandName, String alias) {
            this.commandName = commandName;
            this.alias = alias;
        }

        @Override
        public String commandName() {
            return commandName;
        }

        @Override
        public List<String> aliases() {
            return List.of(commandName, alias);
        }
    }
}
