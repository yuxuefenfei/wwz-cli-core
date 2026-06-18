# wwz-cli-core

`wwz-cli-core` 是一个轻量的交互式 CLI 基础库，用来复用命令解析、命令分发、handler 注册和 JLine 交互式 shell 这套通用流程。

它不包含任何业务命令。业务项目只需要定义自己的命令枚举和 handler，就可以得到一致的交互式命令体验。

## 适用场景

- Spring Boot 命令行工具
- 运维或数据处理类交互式 shell
- 需要 `help`、`clear`、`exit`、历史记录、方向键的内部工具
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
  -> 输出文本
```

## 包结构

| 包 | 职责 |
| --- | --- |
| `com.wwz.cli.core.command` | 命令模型和命令枚举契约 |
| `com.wwz.cli.core.parser` | 把原始输入解析成 `CommandHolder` |
| `com.wwz.cli.core.receiver` | shell 输入接收边界 |
| `com.wwz.cli.core.dispatch` | 命令解析、路由和执行入口 |
| `com.wwz.cli.core.handler` | handler 抽象、注册辅助、系统命令支持 |
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

继承 `CommandHandlerSupport`，在构造函数中注册命令和处理方法。

```java
package com.example.demo.cli;

import com.wwz.cli.core.command.CommandHolder;
import com.wwz.cli.core.handler.CommandHandlerSupport;
import org.springframework.stereotype.Component;

@Component
public class OrgCommandHandler extends CommandHandlerSupport<DemoCommand> {

    public OrgCommandHandler() {
        register(DemoCommand.ORGS, this::listOrgs);
    }

    private String listOrgs(DemoCommand command, CommandHolder holder) {
        var keyword = holder.option("keyword", "");
        var limit = holder.option("limit", "50");
        return "查询机构 keyword=" + keyword + ", limit=" + limit;
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
        return String.join(System.lineSeparator(),
                "可用命令：",
                "  help",
                "  clear | cls",
                "  orgs [--keyword 关键字] [--limit 50]",
                "  exit | quit");
    }
}
```

## 5. 启动交互式 shell

继承 `InteractiveShellRunner`，提供 prompt、历史文件名和启动提示。

```java
package com.example.demo.cli;

import com.wwz.cli.core.dispatch.CommandExecutor;
import com.wwz.cli.core.receiver.CommandReceiver;
import com.wwz.cli.core.shell.InteractiveShellOptions;
import com.wwz.cli.core.shell.InteractiveShellRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Component
public class DemoInteractiveRunner extends InteractiveShellRunner {

    public DemoInteractiveRunner(CommandReceiver commandReceiver, CommandExecutor commandExecutor) {
        super(
                commandReceiver,
                commandExecutor,
                new InteractiveShellOptions(
                        "demo> ",
                        ".demo-cli.history",
                        "demo-cli 已启动，输入 help 查看命令，输入 exit 结束。"
                )
        );
    }

    @Override
    protected boolean debugEnabled(ApplicationArguments args) {
        return args.containsOption("debug");
    }
}
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

示例：

```text
patrol-repair --org-id 1002 --time "2026-06-17 09:00..2026-06-17 10:00" --confirm
```

解析结果：

```text
name    = patrol-repair
args    = []
options = {
  org-id  = 1002
  time    = 2026-06-17 09:00..2026-06-17 10:00
  confirm = true
}
```

## 设计建议

- 命令枚举只表达命令名和别名，不放业务逻辑。
- 一个 handler 管一组相关命令，例如机构、清理、巡检、Redis String。
- 参数校验放在业务 handler 或独立 validator 中。
- 写操作建议统一使用显式选项，例如 `--confirm`。
- `CommandDispatcher` 只负责路由，不负责权限和业务安全策略。
- `InteractiveShellRunner.beforeLoop` 适合做启动前配置校验。
- `InteractiveShellRunner.debugEnabled` 适合控制是否打印异常堆栈。

## 测试建议

- parser 测试：引号、转义、空字符串、布尔开关、`--name=value`。
- resolver 测试：别名、大小写、未知命令。
- dispatcher 测试：重复注册失败、未知命令路由到系统 handler。
- handler 测试：默认预览、确认执行、参数缺失、越权或危险操作拦截。
- shell 子类测试：配置校验 hook、debug 开关。

## 当前已接入项目

- `sdic-sop-data-kit`
- `sdic-sop-redis-cli`
