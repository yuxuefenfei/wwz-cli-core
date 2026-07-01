package com.wwz.cli.core.shell;

import com.wwz.cli.core.command.CommandSpec;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 根据 {@link CommandSpec} 枚举的命令名和别名创建 JLine 补全器。
 *
 * <p>空白名称会被忽略，重复名称只会生成一个候选项。对于空命令、未知命令等不应展示给
 * 用户的内部枚举值，可以通过 {@link #EnumCommandCompleter(Class, Collection)} 显式排除。</p>
 */
public final class EnumCommandCompleter<C extends Enum<C> & CommandSpec> implements Completer {

    private final StringsCompleter delegate;

    /**
     * 使用枚举中的全部命令创建补全器。
     */
    public EnumCommandCompleter(Class<C> enumType) {
        this(enumType, Set.of());
    }

    /**
     * 使用枚举中的命令创建补全器，并排除不应向用户展示的命令。
     *
     * @param enumType 命令枚举类型
     * @param excludedCommands 要排除的枚举值，例如空命令和未知命令
     */
    public EnumCommandCompleter(Class<C> enumType, Collection<C> excludedCommands) {
        Objects.requireNonNull(enumType, "enumType 不能为空");
        Objects.requireNonNull(excludedCommands, "excludedCommands 不能为空");

        var excluded = Set.copyOf(excludedCommands);
        var names = new LinkedHashSet<String>();
        for (C command : enumType.getEnumConstants()) {
            if (excluded.contains(command)) {
                continue;
            }
            addNames(names, command.aliases());
        }
        this.delegate = new StringsCompleter(names);
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (line.wordIndex() != 0) {
            return;
        }
        delegate.complete(reader, line, candidates);
    }

    private static void addNames(Set<String> names, Collection<String> aliases) {
        if (aliases == null) {
            return;
        }
        aliases.stream()
                .filter(Objects::nonNull)
                .filter(alias -> !alias.trim().isEmpty())
                .forEach(names::add);
    }
}
