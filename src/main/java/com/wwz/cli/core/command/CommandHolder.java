package com.wwz.cli.core.command;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parsed command input.
 *
 * <p>A {@code CommandHolder} is the neutral data structure passed between the parser,
 * dispatcher, validators, and command handlers. It intentionally does not know any
 * concrete business command enum, so the same parsed input can be reused by different
 * CLI applications.</p>
 *
 * <p>For a command line such as {@code clean target --org-id=1002 --confirm}, the
 * holder contains:</p>
 *
 * <ul>
 *     <li>{@code name}: {@code clean}</li>
 *     <li>{@code args}: {@code ["target"]}</li>
 *     <li>{@code options}: {@code {"org-id": "1002", "confirm": "true"}}</li>
 * </ul>
 */
public class CommandHolder {

    private final String name;
    private final List<String> args;
    private final Map<String, String> options;

    /**
     * Creates an immutable command holder.
     *
     * <p>The provided lists/maps are wrapped as unmodifiable collections to prevent
     * accidental changes while the command travels through validation and execution.</p>
     */
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

    /**
     * Returns the option value, or the provided default when the option is absent.
     */
    public String option(String name, String defaultValue) {
        return options.getOrDefault(name, defaultValue);
    }

    /**
     * Returns {@code true} when the parsed input contains the option.
     *
     * <p>Boolean switches are represented as {@code optionName=true}, so callers can
     * use this method to check whether a switch such as {@code --confirm} was provided.</p>
     */
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }
}
