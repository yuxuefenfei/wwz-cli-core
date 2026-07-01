package com.wwz.cli.core.shell;

import com.wwz.cli.core.command.CommandSpec;
import org.jline.reader.Candidate;
import org.jline.reader.impl.DefaultParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EnumCommandCompleterTest {

    @Test
    void shouldCompleteCommandNamesAndAliases() {
        var completer = new EnumCommandCompleter<>(TestCommand.class);

        assertThat(candidates(completer))
                .containsExactlyInAnyOrder("help", "h", "list", "ls", "__unknown__");
    }

    @Test
    void shouldExcludeInternalCommandsAndIgnoreBlankAliases() {
        var completer = new EnumCommandCompleter<>(
                TestCommand.class, Set.of(TestCommand.EMPTY, TestCommand.UNKNOWN));

        assertThat(candidates(completer)).containsExactlyInAnyOrder("help", "h", "list", "ls");
    }

    @Test
    void shouldOnlyCompleteTheCommandWord() {
        var completer = new EnumCommandCompleter<>(TestCommand.class);
        var parsedLine = new DefaultParser().parse("help ", 5);
        var candidates = new ArrayList<Candidate>();

        completer.complete(null, parsedLine, candidates);

        assertThat(candidates).isEmpty();
    }

    private List<String> candidates(EnumCommandCompleter<TestCommand> completer) {
        var parsedLine = new DefaultParser().parse("", 0);
        var candidates = new ArrayList<Candidate>();
        completer.complete(null, parsedLine, candidates);
        return candidates.stream().map(Candidate::value).collect(java.util.stream.Collectors.toList());
    }

    enum TestCommand implements CommandSpec {
        EMPTY(""),
        HELP("help", "h", "help"),
        LIST("list", "ls", " "),
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
            var names = new ArrayList<String>();
            names.add(commandName);
            names.addAll(aliases);
            return names;
        }
    }
}
