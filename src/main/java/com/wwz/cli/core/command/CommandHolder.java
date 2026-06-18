package com.wwz.cli.core.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandHolder {

    private final String name;
    private final List<String> args;
    private final Map<String, String> options;

    public CommandHolder(String name, List<String> args, Map<String, String> options) {
        this.name = name;
        this.args = Collections.unmodifiableList(args);
        this.options = Collections.unmodifiableMap(options);
    }

    public String getName() {
        return name;
    }

    public List<String> getArgs() {
        return args;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String option(String name, String defaultValue) {
        return options.getOrDefault(name, defaultValue);
    }

    public boolean hasOption(String name) {
        return options.containsKey(name);
    }
}
