# wwz-cli-core

`wwz-cli-core` 是一个轻量的交互式 CLI 基础库，用来复用命令解析、命令分发、handler 注册、结构化结果、拦截器和 JLine 交互式 shell 这套通用流程。

它不包含任何业务命令。业务项目只需要定义自己的命令枚举和 handler，就可以得到一致的交互式命令体验。

## 适用场景

- Spring Boot 命令行工具
- 运维或数据处理类交互式 shell
- 需要 `help`、`clear`、`exit`、历史记录、方向键和 Tab 补全的内部工具
- 希望避免每个项目重复编写 parser / dispatcher / runner 的项目

## 核心流程

```text
用户输入
  -> CommandReceiver
  -> CommandParser
  -> CommandHolder
  -> CommandResolver
  -> CommandDispatcher
  -> CommandHandler
  -> CommandResult
  -> 输出文本/元数据
```

## 包结构

| 包 | 职责 |
| --- | --- |
| `com.wwz.cli.core.command` | 命令模型、命令枚举契约、结构化执行结果 |
| `com.wwz.cli.core.parser` | 把原始输入解析成 `CommandHolder` |
| `com.wwz.cli.core.receiver` | shell 输入接收边界 |
| `com.wwz.cli.core.dispatch` | 命令解析、路由、执行入口和拦截器 |
| `com.wwz.cli.core.handler` | handler 抽象、注册辅助、系统命令支持 |
| `com.wwz.cli.core.help` | 帮助文本建模和格式化 |
| `com.wwz.cli.core.shell` | 基于 JLine 的 Spring Boot 交互式 shell |

## Maven 引入

```xml
<dependency>
    <groupId>com.wwz</groupId>
    <artifactId>wwz-cli-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

如果该库还没有发布到 Maven 私服，先在 `wwz-cli-core` 项目中执行：

```bash
mvn install
```

## 1. 定义命令枚举

命令枚举实现 `CommandSpec`。建议把空命令、帮助命令、清屏命令、未知命令也放进同一个枚举，方便系统 handler 统一注册。

```java
package com.example.demo.cli;

import com.wwz.cli.core.command.CommandSpec;

import java.util.Arrays;
import java.util.List;

public enum DemoCommand implements CommandSpec {

    EMPTY(""),
    HELP("help"),
    CLEAR("clear", "cls"),
    ORGS("orgs", "list-orgs"),
    UNKNOWN("__unknown__");

    private final String commandName;
    private final String[] aliases;

    DemoCommand(String commandName, String... aliases) {
        this.commandName = commandName;
        this.aliases = aliases;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public List<String> aliases() {
        var all = new java.util.ArrayList<String>();
        all.add(commandName);
        all.addAll(Arrays.asList(aliases));
        return all;
    }
}
```

## 2. 配置基础 Bean

```java
package com.example.demo.cli;

import com.wwz.cli.core.dispatch.CommandDispatcher;
import com.wwz.cli.core.dispatch.CommandExecutor;
import com.wwz.cli.core.dispatch.CommandResolver;
import com.wwz.cli.core.dispatch.EnumCommandResolver;
import com.wwz.cli.core.handler.CommandHandler;
import com.wwz.cli.core.parser.CommandLineParser;
import com.wwz.cli.core.parser.CommandParser;
import com.wwz.cli.core.receiver.CommandReceiver;
import com.wwz.cli.core.receiver.DefaultCommandReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DemoCliConfiguration {

    @Bean
    public CommandParser commandParser() {
        return new CommandLineParser();
    }

    @Bean
    public CommandReceiver commandReceiver(CommandParser commandParser) {
        return new DefaultCommandReceiver(commandParser);
    }

    @Bean
    public CommandResolver<DemoCommand> commandResolver() {
        return new EnumCommandResolver<>(DemoCommand.class, DemoCommand.UNKNOWN);
    }

    @Bean
    public CommandExecutor commandExecutor(CommandResolver<DemoCommand> commandResolver,
                                           List<CommandHandler<DemoCommand>> handlers) {
        return new CommandDispatcher<>(commandResolver, handlers);
    }
}
```

## 3. 编写业务 handler

继承 `CommandHandlerSupport`，在构造函数中注册命令和处理方法。命令操作统一返回 `CommandResult`。

```java
package com.example.demo.cli;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.command.CommandResult;
import com.wwz.cli.core.handler.CommandHandlerSupport;
import org.springframework.stereotype.Component;

@Component
public class OrgCommandHandler extends CommandHandlerSupport<DemoCommand> {

    public OrgCommandHandler() {
        register(DemoCommand.ORGS, this::listOrgs);
    }

    private CommandResult listOrgs(DemoCommand command, CommandHolder holder) {
        var keyword = holder.option("keyword", "");
        var limit = holder.intOption("limit", 50);
        return CommandResult.ok("查询机构 keyword=" + keyword + ", limit=" + limit);
    }
}
```

## 4. 注册系统命令 handler

`SystemCommandHandlerSupport` 已内置空命令、`help`、`clear`、未知命令的通用行为。

```java
package com.example.demo.cli;

import com.wwz.cli.core.handler.SystemCommandHandlerSupport;
import org.springframework.stereotype.Component;

@Component
public class SystemCommandHandler extends SystemCommandHandlerSupport<DemoCommand> {

    public SystemCommandHandler() {
        super(
                DemoCommand.EMPTY,
                DemoCommand.HELP,
                DemoCommand.CLEAR,
                DemoCommand.UNKNOWN,
                this::help
        );
    }

    private String help() {
        return "可用命令：\n  help\n  clear | cls\n  orgs [--keyword 关键字] [--limit 50]\n  exit | quit";
    }
}
```

## 5. 启动交互式 shell

继承 `InteractiveShellRunner`，提供 prompt、历史文件名和启动提示。可按需重写退出钩子和补全器钩子。

```java
package com.example.demo.cli;

import com.wwz.cli.core.dispatch.CommandExecutor;
import com.wwz.cli.core.receiver.CommandReceiver;
import com.wwz.cli.core.shell.InteractiveShellOptions;
import com.wwz.cli.core.shell.InteractiveShellRunner;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.StringsCompleter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoInteractiveRunner extends InteractiveShellRunner {

    public DemoInteractiveRunner(CommandReceiver commandReceiver, CommandExecutor commandExecutor) {
        super(commandReceiver, commandExecutor,
                new InteractiveShellOptions("demo> ", ".demo-cli.history", "demo-cli 已启动，输入 help 查看命令，输入 exit 结束。"));
    }

    @Override
    protected List<Completer> completers() {
        return List.of(new StringsCompleter("help", "clear", "cls", "orgs", "exit", "quit"));
    }

    @Override
    protected boolean beforeExit(String command) {
        return true;
    }

    @Override
    protected boolean debugEnabled(ApplicationArguments args) {
        return args.containsOption("debug");
    }
}
```

## 拦截器

需要审计、计时或统一错误转换时，可以用 `InterceptingCommandExecutor` 包装真实 executor。

```java
var executor = new InterceptingCommandExecutor(
        new CommandDispatcher<>(resolver, handlers),
        List.of(new TimingInterceptor())
);
```

## 帮助文本格式化

```java
var help = HelpFormatter.format(List.of(
        CommandHelpEntry.builder("help").group("系统").description("查看帮助").build(),
        CommandHelpEntry.builder("orgs").group("业务").usage("[--limit 50]").description("查询机构").build()
));
```

## 解析规则

`CommandLineParser` 支持：

- 命令名大小写不敏感
- 位置参数：`get user:1`
- `--name=value`
- `--name value`
- 布尔开关：`--confirm` 会解析为 `confirm=true`
- 单引号和双引号：`--time "2026-06-17 09:00"`
- 反斜杠转义
- 空引号值：`--reason ""`
- 末尾单独反斜杠会按字面量保留

## 工程化

- `mvn test` 执行单元测试并生成 JaCoCo 执行数据。
- `mvn verify` 生成 JaCoCo HTML/XML 报告。
- `mvn package` 生成主 jar 和 `sources.jar`。
- `.github/workflows/ci.yml` 提供最小 CI 验证。