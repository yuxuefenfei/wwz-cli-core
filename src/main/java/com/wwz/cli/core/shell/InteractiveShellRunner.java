package com.wwz.cli.core.shell;

import com.wwz.cli.core.dispatch.CommandExecutor;
import com.wwz.cli.core.receiver.CommandReceiver;

import org.jline.keymap.KeyMap;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.TerminalBuilder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.nio.file.Paths;
import java.util.Set;

/**
 * Base Spring Boot runner for a persistent interactive CLI shell.
 *
 * <p>The runner owns terminal concerns: prompt display, JLine history, arrow-key support,
 * exit/quit handling, and exception printing. It delegates command semantics to
 * {@link CommandReceiver} and {@link CommandExecutor}, which keeps business applications
 * focused on their command handlers.</p>
 *
 * <p>Typical usage is to create an application-specific subclass and expose it as a
 * Spring bean. Override {@link #beforeLoop(ApplicationArguments)} for startup validation
 * and {@link #debugEnabled(ApplicationArguments)} when stack traces should be printed.</p>
 */
public abstract class InteractiveShellRunner implements ApplicationRunner {

    private static final String ANSI_UP = "\033[A";
    private static final String ANSI_DOWN = "\033[B";
    private static final String ANSI_RIGHT = "\033[C";
    private static final String ANSI_LEFT = "\033[D";
    private static final String APPLICATION_UP = "\033OA";
    private static final String APPLICATION_DOWN = "\033OB";
    private static final String APPLICATION_RIGHT = "\033OC";
    private static final String APPLICATION_LEFT = "\033OD";
    private static final Set<String> ARROW_KEY_ESCAPES = Set.of(
            ANSI_UP, ANSI_DOWN, ANSI_RIGHT, ANSI_LEFT,
            APPLICATION_UP, APPLICATION_DOWN, APPLICATION_RIGHT, APPLICATION_LEFT);

    private final CommandReceiver commandReceiver;
    private final CommandExecutor commandExecutor;
    private final InteractiveShellOptions options;

    /**
     * Creates a shell runner.
     */
    protected InteractiveShellRunner(CommandReceiver commandReceiver,
                                     CommandExecutor commandExecutor,
                                     InteractiveShellOptions options) {
        this.commandReceiver = commandReceiver;
        this.commandExecutor = commandExecutor;
        this.options = options;
    }

    /**
     * Starts the interactive loop.
     *
     * <p>Each non-empty line is parsed and executed. The returned output is printed as-is.
     * Exceptions are converted to a short user-facing failure message, with optional stack
     * traces controlled by {@link #debugEnabled(ApplicationArguments)}.</p>
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        beforeLoop(args);
        System.out.println(options.startupMessage());
        try (var terminal = TerminalBuilder.builder().system(true).build()) {
            var reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), options.historyFileName()))
                    .build();
            bindArrowKeys(reader);
            while (true) {
                String line;
                try {
                    line = reader.readLine(options.prompt());
                } catch (UserInterruptException ex) {
                    continue;
                } catch (EndOfFileException ex) {
                    break;
                }
                var trimmed = line.trim();
                if (trimmed.isEmpty() || isArrowKeyEscape(trimmed)) {
                    continue;
                }
                if ("exit".equalsIgnoreCase(trimmed) || "quit".equalsIgnoreCase(trimmed)) {
                    System.out.println("已结束。");
                    break;
                }
                try {
                    var output = commandExecutor.execute(commandReceiver.receive(trimmed));
                    if (!output.isEmpty()) {
                        System.out.println(output);
                    }
                } catch (Exception ex) {
                    System.out.println("操作失败：" + ex.getMessage());
                    if (debugEnabled(args)) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }
    }

    /**
     * Hook invoked after Spring starts but before the first prompt is shown.
     *
     * <p>Applications can validate required configuration here, for example Redis nodes
     * or database connection settings.</p>
     */
    protected void beforeLoop(ApplicationArguments args) throws Exception {
    }

    /**
     * Controls whether command failures print stack traces to the console.
     */
    protected boolean debugEnabled(ApplicationArguments args) {
        return false;
    }

    private void bindArrowKeys(LineReader reader) {
        KeyMap<org.jline.reader.Binding> main = reader.getKeyMaps().get(LineReader.MAIN);
        if (main == null) {
            return;
        }
        main.bind(new Reference(LineReader.UP_LINE_OR_HISTORY), ANSI_UP, APPLICATION_UP);
        main.bind(new Reference(LineReader.DOWN_LINE_OR_HISTORY), ANSI_DOWN, APPLICATION_DOWN);
        main.bind(new Reference(LineReader.FORWARD_CHAR), ANSI_RIGHT, APPLICATION_RIGHT);
        main.bind(new Reference(LineReader.BACKWARD_CHAR), ANSI_LEFT, APPLICATION_LEFT);
    }

    private boolean isArrowKeyEscape(String text) {
        return ARROW_KEY_ESCAPES.contains(text);
    }
}
