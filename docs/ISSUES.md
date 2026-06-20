# wwz-cli-core 问题集

> 本文档基于 2026-06-20 对项目 `wwz-cli-core` 主分支的全量代码审查整理，按严重程度和影响范围分级。

---
## 验证与修复状态

| 编号 | 验证结果 | 修复状态 | 修复摘要 |
|------|----------|----------|----------|
| 1 | 存在 | 已修复 | `EnumCommandResolver` 对跨命令别名冲突启动期失败 |
| 2 | 存在 | 已修复 | `InteractiveShellRunner` 增加退出命令、退出前后钩子和退出文案钩子 |
| 3 | 存在 | 已修复 | `CommandHolder` 增加位置参数、整数选项、布尔选项便捷方法 |
| 4 | 存在 | 已修复 | 单元测试从 5 个补充到 36 个，覆盖核心组件正常和异常路径 |
| 5 | 存在 | 已修复 | `InteractiveShellRunner` 增加 `Completer` 扩展点 |
| 6 | 存在 | 已修复 | shell 运行时接入 SLF4J 日志，用户输出仍保留在控制台 |
| 7 | 存在 | 已修复 | 新增 `CommandInterceptor` 和 `InterceptingCommandExecutor` |
| 8 | 存在 | 已修复 | 新增 `CommandHelpEntry` 和 `HelpFormatter` |
| 9 | 存在 | 已修复 | 核心执行链统一改为返回 `CommandResult` |
| 10 | 存在 | 已修复 | 新增 `.github/workflows/ci.yml` |
| 11 | 存在 | 已修复 | JLine 从 `3.14.0` 升级到 `3.27.1` |
| 12 | 存在 | 已修复 | pom 增加 `jacoco-maven-plugin`，`verify` 阶段生成报告 |
| 13 | 存在 | 已修复 | 为末尾反斜杠行为补测试和源码注释，明确按字面量保留 |

---
## 目录

- [P0 — 缺陷与风险](#p0--缺陷与风险)
  - [1. EnumCommandResolver 别名冲突静默覆盖](#1-enumcommandresolver-别名冲突静默覆盖)
- [P1 — 设计缺口](#p1--设计缺口)
  - [2. InteractiveShellRunner 中 exit/quit 硬编码](#2-interactiveshellrunner-中-exitquit-硬编码)
  - [3. CommandHolder 缺少便捷方法](#3-commandholder-缺少便捷方法)
  - [4. 测试覆盖严重不足](#4-测试覆盖严重不足)
- [P2 — 功能增强](#p2--功能增强)
  - [5. 缺少 Tab 补全支持](#5-缺少-tab-补全支持)
  - [6. 缺少日志框架集成](#6-缺少日志框架集成)
  - [7. 缺少命令执行拦截器钩子](#7-缺少命令执行拦截器钩子)
  - [8. 缺少帮助文本格式化工具](#8-缺少帮助文本格式化工具)
  - [9. 缺少结构化的命令执行结果](#9-缺少结构化的命令执行结果)
- [P3 — 工程化与维护](#p3--工程化与维护)
  - [10. 缺少 CI/CD 构建流水线](#10-缺少-cicd-构建流水线)
  - [11. Spring Boot 与 JLine 版本陈旧](#11-spring-boot-与-jline-版本陈旧)
  - [12. 缺少代码覆盖率度量](#12-缺少代码覆盖率度量)
  - [13. CommandLineParser 末尾反斜杠行为未明确](#13-commandlineparser-末尾反斜杠行为未明确)

---

## P0 — 缺陷与风险

### 1. EnumCommandResolver 别名冲突静默覆盖

**位置**：`src/main/java/com/wwz/cli/core/dispatch/EnumCommandResolver.java:30`

**问题描述**：

在构造索引表时，对不同枚举值使用了相同的别名会静默覆盖，不报错：

```java
lookup.put(alias.toLowerCase(Locale.ROOT), command);
```

例如：

```java
public enum DemoCommand implements CommandSpec {
    HELLO("hello", "hi"),
    HELP("help", "hi");     // ← 别名 "hi" 重复，HELLO 将被覆盖
}
```

此时输入 `hi` 会路由到 `HELP` 而非 `HELLO`，且启动时没有任何错误提示。

**影响**：
- 与项目的"启动期快速失败"设计哲学矛盾（同 handler 内重复注册会抛 `IllegalStateException`，但跨命令的别名冲突却静默吞掉）
- 业务方在增加新命令别名时，可能无意覆盖了已有命令的别名，导致线上路由错误

**建议修复**：

```java
for (C command : enumType.getEnumConstants()) {
    for (String alias : command.aliases()) {
        var previous = lookup.put(alias.toLowerCase(Locale.ROOT), command);
        if (previous != null && previous != command) {
            throw new IllegalStateException(
                "别名冲突: " + alias + " 同时映射到 " + previous.commandName()
                    + " 和 " + command.commandName());
        }
    }
}
```

需要排除 `previous == command` 的情况，因为同一个枚举值的 `commandName()` 和 `aliases()` 可能返回重复值。

---

## P1 — 设计缺口

### 2. InteractiveShellRunner 中 exit/quit 硬编码

**位置**：`src/main/java/com/wwz/cli/core/shell/InteractiveShellRunner.java:88-91`

**问题描述**：

```java
if ("exit".equalsIgnoreCase(trimmed) || "quit".equalsIgnoreCase(trimmed)) {
    System.out.println("已结束。");
    break;
}
```

exit/quit 被直接拦截在命令系统之外，与 help/clear 的处理方式不一致：

| 命令 | 处理方式 | 是否可自定义 |
|------|---------|-------------|
| help | `SystemCommandHandlerSupport.help()` | ✅ 通过 supplier |
| clear | `SystemCommandHandlerSupport.clear()` | ✅ 返回 ANSI |
| 未知命令 | `SystemCommandHandlerSupport.unknown()` | ✅ 可重写 |
| exit/quit | `InteractiveShellRunner` 硬编码 | ❌ 不可 |

**影响**：
- 业务方无法在退出前执行清理（如关闭数据库连接、保存状态）
- exit 命令不会出现在帮助文本中（业务方需额外写死）
- 无法支持退出确认（如"确定要退出吗？ y/n"）

**建议方案**（二选一）：

**方案 A**：将 exit 交给 `SystemCommandHandlerSupport` 处理，通过一个 `beforeExit` 钩子扩展：

```java
// SystemCommandHandlerSupport 中增加退出命令注册
register(exitCommand, this::exit);
this.exitCommand = exitCommand;

// InteractiveShellRunner 通过 CommandResult 或返回值约定来通知退出
```

**方案 B**：在 shell runner 中增加钩子：

```java
protected boolean beforeExit() {
    return true;  // 返回 false 取消退出
}

protected void afterExit() {
    // 退出前清理
}
```

---

### 3. CommandHolder 缺少便捷方法

**位置**：`src/main/java/com/wwz/cli/core/command/CommandHolder.java`

**问题描述**：

当前 `CommandHolder` 只提供了基础的 getter 和 `option(name, default)` 方法。业务 handler 经常需要按索引取位置参数、或取类型化的 option 值，目前只能自行处理：

```java
// 当前的写法
var keyword = holder.option("limit", "50");
var limit = Integer.parseInt(keyword);  // 需要自行转换和异常处理

// 位置参数取不到
var target = holder.getArgs().get(0);  // 可能 IndexOutOfBoundsException
```

**建议新增方法**：

```java
/**
 * 按索引获取位置参数，超出范围时返回默认值。
 */
public String arg(int index) {
    return index < args.size() ? args.get(index) : null;
}

/**
 * 按索引获取位置参数，超出范围时返回指定默认值。
 */
public String arg(int index, String defaultValue) {
    return index < args.size() ? args.get(index) : defaultValue;
}

/**
 * 位置参数数量。
 */
public int argCount() {
    return args.size();
}

/**
 * 获取整数类型的选项值。
 */
public int intOption(String name, int defaultValue) {
    var value = options.get(name);
    if (value == null) return defaultValue;
    try {
        return Integer.parseInt(value);
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException("选项 " + name + " 需要为整数，实际值: " + value);
    }
}

/**
 * 获取布尔类型的选项值（"--flag" 或 "--flag=true" 视为 true）。
 */
public boolean boolOption(String name) {
    var value = options.get(name);
    return value != null && !"false".equalsIgnoreCase(value);
}
```

---

### 4. 测试覆盖严重不足

**位置**：`src/test/java/com/wwz/cli/core/`

**现状与差距**：

| 组件 | 已有用例数 | 缺失的关键场景 |
|------|-----------|-------------|
| `CommandLineParser` | 3 | 空输入、null 输入、单引号字符串、反斜杠转义、多位置参数、纯命令名、`--name value` 形式、多个混合选项、仅 `--` 输入、大小写验证 |
| `EnumCommandResolver` | 0 | 大小写不敏感、别名匹配、别名冲突报错、null 输入、空白输入、未知命令回退 |
| `CommandHandlerSupport` | 0 | 注册 → 执行、重复注册抛异常、调用未注册 command 抛异常 |
| `SystemCommandHandlerSupport` | 0 | 空命令返回空串、help 调用 supplier、clear 返回 ANSI、unknown 返回提示文案 |
| `CommandDispatcher` | 2 | 正常路由到业务 handler、handler 执行抛异常时的传播行为 |
| `DefaultCommandReceiver` | 0 | 委托给 parser 的正确性 |
| `InteractiveShellRunner` | 0 | （shell 测试通常需要集成测试，但至少应测试核心逻辑） |

**建议**：

为每个组件补齐下表所列的测试用例，目标每个组件至少覆盖正常路径 + 关键异常路径。

<details>
<summary>点击展开建议的测试用例清单</summary>

**CommandLineParserTest**

- `shouldReturnEmptyCommandForNullInput`
- `shouldReturnEmptyCommandForBlankInput`
- `shouldTreatFirstTokenAsName`
- `shouldCollectPositionalArgs`
- `shouldParseOptionsWithEqualsSign`
- `shouldParseOptionsWithSpaceSeparator`
- `shouldParseBooleanSwitchAsTrue`
- `shouldHandleSingleQuotedValue`
- `shouldHandleDoubleQuotedValue`
- `shouldHandleBackslashEscape`
- `shouldHandleMixedQuotedAndBooleanOptions`
- `shouldRejectUnclosedSingleQuote`
- `shouldPreserveEmptyQuotedOption`

**EnumCommandResolverTest**

- `shouldResolveByCommandName`
- `shouldResolveByAlias`
- `shouldResolveCaseInsensitively`
- `shouldResolveWithLeadingTrailingWhitespace`
- `shouldReturnUnknownForNullInput`
- `shouldReturnUnknownForUnrecognizedName`
- `shouldRejectDuplicateAliasAcrossCommands`

**CommandHandlerSupportTest**

- `shouldReturnSupportedCommands`
- `shouldExecuteRegisteredOperation`
- `shouldRejectDuplicateRegistration`
- `shouldThrowForUnregisteredCommand`

**SystemCommandHandlerSupportTest**

- `shouldReturnEmptyStringForEmptyCommand`
- `shouldDelegateHelpToSupplier`
- `shouldReturnAnsiClearScreenForClearCommand`
- `shouldReturnUnknownMessageForUnknownCommand`

**CommandDispatcherTest（补充）**

- `shouldRouteToMatchingHandler`
- `shouldPropagateHandlerException`

**DefaultCommandReceiverTest**

- `shouldDelegateToParser`

</details>

---

## P2 — 功能增强

### 5. 缺少 Tab 补全支持

**位置**：`src/main/java/com/wwz/cli/core/shell/InteractiveShellRunner.java`

**问题描述**：

JLine 原生支持 `Completer` 接口，但框架完全没有接入。对于一个交互式 shell，没有 Tab 补全会降低用户体验。

**建议方案**：

在 `InteractiveShellRunner` 中通过模板方法暴露补全器，让业务方按需注入：

```java
/**
 * 返回 JLine 补全器列表。默认返回空列表（无补全）。
 * 业务方重写此方法可以提供命令名和选项的补全。
 */
protected List<Completer> completers() {
    return List.of();
}
```

在 `LineReaderBuilder` 构建时注入：

```java
var builder = LineReaderBuilder.builder()
        .terminal(terminal)
        .variable(LineReader.HISTORY_FILE, ...);

for (Completer completer : completers()) {
    builder.completer(completer);
}
```

可进一步提供一个基于命令枚举自动生成命令名补全的默认实现。

---

### 6. 缺少日志框架集成

**位置**：全部模块

**问题描述**：

整个框架零日志记录。所有输出都通过 `System.out.println` 或 `System.err`：

- `InteractiveShellRunner` 的异常信息 → stdout
- `InteractiveShellRunner` 的启动消息 → stdout
- debug 模式下的堆栈 → stdout

在一个生产工具中，启动信息、命令执行日志应接入日志框架（SLF4J），以便：
- 区分日志级别（info / debug / error）
- 输出到文件或日志中心
- 按运行环境控制日志量

**建议方案**：

1. 加入 `spring-boot-starter-logging`（已有的 spring-boot 依赖已间接引入 SLF4J + Logback）
2. 在关键位置增加日志点：

```java
private static final Logger log = LoggerFactory.getLogger(InteractiveShellRunner.class);

// 启动
log.info("交互式 shell 已启动");

// 异常
log.error("命令执行失败: {}", commandHolder.getName(), ex);

// debug 模式
if (log.isDebugEnabled()) {
    log.debug("执行命令: {}", commandHolder.getName());
}
```

3. 将面向用户的提示保持为 `System.out`，将诊断信息改为日志

---

### 7. 缺少命令执行拦截器钩子

**位置**：`src/main/java/com/wwz/cli/core/dispatch/CommandDispatcher.java`

**问题描述**：

业务方如需在每条命令执行前后做统一处理（审计日志、执行耗时、权限校验），目前只能在每个 handler 中重复编写。修改 `CommandReceiver` 也不合适——那是在解析之前。

**建议方案**：

定义一个拦截器接口，在 `CommandExecutor` 层面通过装饰器链执行：

```java
public interface CommandInterceptor {

    /**
     * 在命令执行前调用。可修改或替换 CommandHolder。
     */
    default CommandHolder beforeExecute(CommandHolder holder) {
        return holder;
    }

    /**
     * 在命令成功执行后调用。可修改输出。
     */
    default String afterExecute(CommandHolder holder, String output) {
        return output;
    }

    /**
     * 在命令执行抛出异常时调用。返回替代输出或重新抛出。
     */
    default String onError(CommandHolder holder, Exception ex) {
        throw new RuntimeException(ex);
    }
}
```

可选：提供一个带拦截器的 `CommandExecutor` 包装类：

```java
public class InterceptingCommandExecutor implements CommandExecutor {
    private final CommandExecutor delegate;
    private final List<CommandInterceptor> interceptors;

    @Override
    public String execute(CommandHolder holder) {
        var h = holder;
        for (var interceptor : interceptors) {
            h = interceptor.beforeExecute(h);
        }
        try {
            var output = delegate.execute(h);
            for (var interceptor : interceptors) {
                output = interceptor.afterExecute(h, output);
            }
            return output;
        } catch (Exception e) {
            for (var interceptor : interceptors) {
                return interceptor.onError(h, e);  // 第一个匹配的拦截器处理
            }
            throw e;
        }
    }
}
```

业务方使用示例（计时拦截器）：

```java
@Component
public class TimingInterceptor implements CommandInterceptor {
    private final ThreadLocal<Long> start = new ThreadLocal<>();

    @Override
    public CommandHolder beforeExecute(CommandHolder holder) {
        start.set(System.currentTimeMillis());
        return holder;
    }

    @Override
    public String afterExecute(CommandHolder holder, String output) {
        var elapsed = System.currentTimeMillis() - start.get();
        return output + "\n[耗时 " + elapsed + "ms]";
    }
}
```

---

### 8. 缺少帮助文本格式化工具

**位置**：无对应模块

**问题描述**：

当前帮助文本完全由业务方手工拼接（见 README 示例）：

```java
private String help() {
    return String.join(System.lineSeparator(),
            "可用命令：",
            "  help",
            "  clear | cls",
            "  orgs [--keyword 关键字] [--limit 50]",
            "  exit | quit");
}
```

命令多了之后，文本会很杂乱，且每个业务工程都要重复写拼接逻辑。

**建议方案**：

提供一个轻量的帮助格式化器：

```java
package com.wwz.cli.core.util;

public class HelpFormatter {

    /**
     * 按分组格式化帮助文本。
     */
    public static String format(List<CommandHelpEntry> entries) {
        var sb = new StringBuilder();
        // 自动计算列宽、左对齐、分组标题
        // ...
        return sb.toString();
    }

    public static class CommandHelpEntry {
        private final String name;
        private final String aliases;
        private final String description;
        private final String usage;
        // builder ...
    }
}
```

---

### 9. 缺少结构化的命令执行结果

**位置**：`CommandExecutor.execute()` / `CommandOperation.execute()` 返回类型均为 `String`

**问题描述**：

`String` 无法区分"正常但无输出"和"正常的空结果"。也无法传递结构化信息（如错误码、后续操作提示）。

**建议方案**：

定义轻量结果对象：

```java
public class CommandResult {

    private final boolean success;
    private final String output;
    private final Map<String, Object> metadata;

    public static CommandResult ok(String output) {
        return new CommandResult(true, output, Map.of());
    }

    public static CommandResult error(String message) {
        return new CommandResult(false, message, Map.of());
    }

    // getters ...
}
```

**兼容性考量**：若要保持现有 `CommandOperation` 返回 `String` 的约定，可以仅在 `CommandExecutor` 层面引入 `CommandResult`，让 handler 仍然只返回文本，由 dispatcher 包装为成功结果。向后兼容成本低。

---

## P3 — 工程化与维护

### 10. 缺少 CI/CD 构建流水线

**位置**：`.github/` 目录仅包含 modernize hooks，无构建/测试 pipeline。

**建议方案**：

添加最小 GitHub Actions 流水线：

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven
      - run: mvn --batch-mode verify
```

---

### 11. JLine 版本陈旧

| 依赖 | 当前版本 | 最新稳定版 | 备注 |
|------|---------|-----------|------|
| JLine | 3.14.0 | 3.27.x | 2018 → 2025 五年未升级 |

**建议**：

- **短期**：至少升级 JLine 到 3.25+，获取更好的终端兼容性（特别是 Windows Terminal / WSL2 / VS Code 内置终端）

---

### 12. 缺少代码覆盖率度量

**现状**：pom.xml 中未配置 JaCoCo 或任何覆盖率插件。

**建议方案**：

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

结合 CI 设置覆盖率门槛（如行覆盖率 ≥ 70%）。

---

### 13. CommandLineParser 末尾反斜杠行为未明确

**位置**：`src/main/java/com/wwz/cli/core/parser/CommandLineParser.java:111-114`

**问题描述**：

```java
if (escaped) {
    current.append('\\');
    tokenStarted = true;
}
```

当输入以未转义的反斜杠结尾时（如 `get user\`），解析器会将 `\` 作为字面量追加到 token。该行为是故意的容错处理还是代码遗留不够清晰——没有测试覆盖、没有注释说明。

**建议**：

1. 为该行为补充一个测试用例，明确这是设计意图
2. 如果设计意图就是容错，则在源码中加注释说明
3. 如果认为不合法，应在 tokenize 方法中抛 `IllegalArgumentException`

---

## 变更记录

| 日期 | 变更内容 |
|------|---------|
| 2026-06-20 | 初始版本，基于主分支全量代码审查 |
