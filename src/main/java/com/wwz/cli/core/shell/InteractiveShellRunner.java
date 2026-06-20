package com.wwz.cli.core.shell;

import com.wwz.cli.core.dispatch.CommandExecutor;
import com.wwz.cli.core.receiver.CommandReceiver;

import org.jline.keymap.KeyMap;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.Reference;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * 持续交互式 CLI shell 的 Spring Boot runner 基类。
 */
public abstract class InteractiveShellRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InteractiveShellRunner.class);

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
     * 创建 shell runner。
     */
    protected InteractiveShellRunner(CommandReceiver commandReceiver,
                                     CommandExecutor commandExecutor,
                                     InteractiveShellOptions options) {
        this.commandReceiver = commandReceiver;
        this.commandExecutor = commandExecutor;
        this.options = options;
    }

    /**
     * 启动交互式循环。
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        beforeLoop(args);
        System.out.println(options.startupMessage());
        log.info("交互式 shell 已启动");
        try (var terminal = TerminalBuilder.builder().system(true).build()) {
            var builder = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .variable(LineReader.HISTORY_FILE, Paths.get(System.getProperty("user.home"), options.historyFileName()));
            configureCompleters(builder);
            var reader = builder.build();
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
                if (isExitCommand(trimmed)) {
                    if (beforeExit(trimmed)) {
                        System.out.println(exitMessage(trimmed));
                        afterExit(trimmed);
                        break;
                    }
                    continue;
                }
                try {
                    log.debug("执行命令：{}", trimmed);
                    var result = commandExecutor.execute(commandReceiver.receive(trimmed));
                    if (result.hasOutput()) {
                        System.out.println(result.getOutput());
                    }
                } catch (Exception ex) {
                    log.error("命令执行失败：{}", trimmed, ex);
                    System.out.println("操作失败：" + ex.getMessage());
                    if (debugEnabled(args)) {
                        ex.printStackTrace(System.out);
                    }
                }
            }
        }
    }

    /**
     * Spring 启动后、首次展示提示符前调用的钩子方法。
     */
    protected void beforeLoop(ApplicationArguments args) throws Exception {
    }

    /**
     * 返回 JLine 补全器列表。业务应用可重写该方法提供命令名和选项补全。
     */
    protected List<Completer> completers() {
        return List.of();
    }

    /**
     * 返回退出命令列表。业务应用可重写该方法支持更多退出别名。
     */
    protected List<String> exitCommands() {
        return List.of("exit", "quit");
    }

    /**
     * 判断当前输入是否为退出命令。
     */
    protected boolean isExitCommand(String text) {
        return exitCommands().stream().anyMatch(command -> command.equalsIgnoreCase(text));
    }

    /**
     * 退出前调用，返回 {@code false} 可取消退出。
     */
    protected boolean beforeExit(String command) {
        return true;
    }

    /**
     * 退出后调用，适合释放资源或保存状态。
     */
    protected void afterExit(String command) {
    }

    /**
     * 退出时打印的用户提示。
     */
    protected String exitMessage(String command) {
        return "已结束。";
    }

    /**
     * 控制命令执行失败时是否在控制台打印异常堆栈。
     */
    protected boolean debugEnabled(ApplicationArguments args) {
        return false;
    }

    private void configureCompleters(LineReaderBuilder builder) {
        var configuredCompleters = completers();
        if (configuredCompleters.isEmpty()) {
            return;
        }
        builder.completer((reader, line, candidates) -> {
            for (Completer completer : configuredCompleters) {
                completer.complete(reader, line, candidates);
            }
        });
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
