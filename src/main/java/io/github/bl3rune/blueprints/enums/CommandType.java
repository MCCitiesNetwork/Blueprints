package io.github.bl3rune.blueprints.enums;

/**
 * Declarative metadata for the plugin's commands. After Phase 5 of the
 * architecture overhaul this enum no longer instantiates handler classes;
 * executor + tab-completer instances are constructed in
 * {@code PluginBootstrap} (with explicit dependencies) and held by
 * {@code CommandRegistry}. The enum keeps the public command name, full
 * command name (including the {@code blueprints.} prefix where applicable),
 * and a flag advertising whether a tab completer is available.
 */
public enum CommandType {

    BLU3PRINT("blueprints", false),
    DUPLICATE("duplicate", false),
    FACE("face", true),
    ROTATE("rotate", true),
    TURN("turn", true),
    IMPORT("import", false),
    EXPORT("export", false),
    SCALE("scale", true),
    NAME("name", false),
    GIVE("give", true),
    HELP("help", false),
    CONFIG("config", true),
    PLAYER("player-config", true),
    GLOBAL("global-config", true);

    private final String commandString;
    private final boolean hasTabCompleter;

    CommandType(String commandString, boolean hasTabCompleter) {
        this.commandString = commandString;
        this.hasTabCompleter = hasTabCompleter;
    }

    @Override
    public String toString() {
        return commandString;
    }

    public boolean hasTabCompleter() {
        return hasTabCompleter;
    }

    public String getFullCommandName() {
        if (commandString.equals("blueprints")) {
            return commandString;
        }
        return "blueprints." + commandString;
    }
}
