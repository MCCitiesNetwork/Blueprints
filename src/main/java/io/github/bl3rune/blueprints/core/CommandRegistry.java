package io.github.bl3rune.blueprints.core;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import io.github.bl3rune.blueprints.enums.CommandType;

/**
 * Holds the constructed command executor and (optional) tab completer for
 * each {@link CommandType}. After Phase 5, {@code CommandType} is a pure
 * metadata enum and handler construction happens in {@code PluginBootstrap}
 * so dependencies are injected explicitly instead of being looked up via
 * {@code Blueprints.getInstance()} inside zero-argument constructors.
 */
public final class CommandRegistry {

    private final Map<CommandType, CommandExecutor> executors = new EnumMap<>(CommandType.class);
    private final Map<CommandType, TabCompleter> tabCompleters = new EnumMap<>(CommandType.class);

    public void register(CommandType type, CommandExecutor executor) {
        executors.put(type, executor);
    }

    public void register(CommandType type, CommandExecutor executor, TabCompleter tabCompleter) {
        executors.put(type, executor);
        if (tabCompleter != null) {
            tabCompleters.put(type, tabCompleter);
        }
    }

    public CommandExecutor getExecutor(CommandType type) {
        return executors.get(type);
    }

    public TabCompleter getTabCompleter(CommandType type) {
        return tabCompleters.get(type);
    }
}
