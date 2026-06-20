package com.wwz.cli.core.help;

import java.util.List;

/**
 * 单条命令帮助信息。
 */
public class CommandHelpEntry {

    private final String group;
    private final String name;
    private final List<String> aliases;
    private final String usage;
    private final String description;

    private CommandHelpEntry(Builder builder) {
        this.group = builder.group;
        this.name = builder.name;
        this.aliases = List.copyOf(builder.aliases);
        this.usage = builder.usage;
        this.description = builder.description;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public static class Builder {
        private String group = "默认";
        private final String name;
        private List<String> aliases = List.of();
        private String usage = "";
        private String description = "";

        private Builder(String name) {
            this.name = name;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder aliases(List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public CommandHelpEntry build() {
            return new CommandHelpEntry(this);
        }
    }
}
