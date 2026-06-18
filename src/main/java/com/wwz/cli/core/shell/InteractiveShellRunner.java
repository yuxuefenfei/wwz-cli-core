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
 * 持续交互式 CLI shell 的 Spring Boot runner 基类。
 *
 * <p>该 runner 负责终端层面的通用能力：提示符显示、JLine 历史记录、方向键支持、
 * exit/quit 退出处理和异常打印。命令语义会委托给 {@link CommandReceiver} 和
 * {@link CommandExecutor}，从而让业务应用只关注自己的命令 handler。</p>
 *
 * <p>典型用法是创建一个业务应用自己的子类，并将其注册为 Spring bean。需要启动前校验时
 * 重写 {@link #beforeLoop(ApplicationArguments)}；需要控制是否打印异常堆栈时重写
 * {@link #debugEnabled(ApplicationArguments)}。</p>
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
     *
     * <p>每一行非空输入都会被解析并执行。执行结果会原样打印。异常会被转换为简短的用户提示，
     * 是否额外打印异常堆栈由 {@link #debugEnabled(ApplicationArguments)} 控制。</p>
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
     * Spring 启动后、首次展示提示符前调用的钩子方法。
     *
     * <p>业务应用可以在这里校验必要配置，例如连接节点或数据库连接参数。</p>
     */
    protected void beforeLoop(ApplicationArguments args) throws Exception {
    }

    /**
     * 控制命令执行失败时是否在控制台打印异常堆栈。
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
