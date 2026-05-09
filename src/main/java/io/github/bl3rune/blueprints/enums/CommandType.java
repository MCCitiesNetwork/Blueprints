package io.github.bl3rune.blueprints.enums;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;

import io.github.bl3rune.blueprints.commands.Blu3printCommand;
import io.github.bl3rune.blueprints.commands.ConfigCommand;
import io.github.bl3rune.blueprints.commands.ConfigTabCompleter;
import io.github.bl3rune.blueprints.commands.FaceCommand;
import io.github.bl3rune.blueprints.commands.FaceTabCompleter;
import io.github.bl3rune.blueprints.commands.ScaleCommand;
import io.github.bl3rune.blueprints.commands.ScaleTabCompleter;
import io.github.bl3rune.blueprints.commands.TurnCommand;
import io.github.bl3rune.blueprints.commands.TurnTabCompleter;
import io.github.bl3rune.blueprints.commands.DuplicateCommand;
import io.github.bl3rune.blueprints.commands.ExportCommand;
import io.github.bl3rune.blueprints.commands.GiveCommand;
import io.github.bl3rune.blueprints.commands.GiveTabCompleter;
import io.github.bl3rune.blueprints.commands.GlobalConfigCommand;
import io.github.bl3rune.blueprints.commands.GlobalConfigTabCompleter;
import io.github.bl3rune.blueprints.commands.HelpCommand;
import io.github.bl3rune.blueprints.commands.ImportCommand;
import io.github.bl3rune.blueprints.commands.NameCommand;
import io.github.bl3rune.blueprints.commands.PlayerConfigCommand;
import io.github.bl3rune.blueprints.commands.PlayerConfigTabCompleter;
import io.github.bl3rune.blueprints.commands.RotateCommand;
import io.github.bl3rune.blueprints.commands.RotateTabCompleter;

/**
 * Enum for the different command types.
 */
public enum CommandType {

    BLU3PRINT("blueprints", new Blu3printCommand()),
    DUPLICATE("duplicate", new DuplicateCommand()),
    FACE("face", new FaceCommand(), new FaceTabCompleter()),
    ROTATE("rotate", new RotateCommand(), new RotateTabCompleter()),
    TURN("turn", new TurnCommand(), new TurnTabCompleter()),
    IMPORT("import", new ImportCommand()),
    EXPORT("export", new ExportCommand()),
    SCALE("scale", new ScaleCommand(), new ScaleTabCompleter()),
    NAME("name", new NameCommand()),
    GIVE("give", new GiveCommand(), new GiveTabCompleter()),
    HELP("help", new HelpCommand()),
    CONFIG("config", new ConfigCommand(), new ConfigTabCompleter()),
    PLAYER("player-config", new PlayerConfigCommand(), new PlayerConfigTabCompleter()),
    GLOBAL("global-config", new GlobalConfigCommand(), new GlobalConfigTabCompleter())
    ;

    private final String commandString;
    private final CommandExecutor commandExecutor;
    private final TabCompleter tabCompleter;

    CommandType(String commandString, CommandExecutor commandExecutor) {
        this.commandString = commandString;
        this.commandExecutor = commandExecutor;
        this.tabCompleter = null;
    }

    CommandType(String commandString, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        this.commandString = commandString;
        this.commandExecutor = commandExecutor;
        this.tabCompleter = tabCompleter;
    }

    @Override
    public String toString() {
        return commandString;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public TabCompleter getTabCompleter() {
        return tabCompleter;
    }

    /**
     * Returns the full command name, including the plugin name.
     * @return The full command name.
     */
    public String getFullCommandName() {
        if (commandString.equals("blueprints")) {
            return commandString;
        }
        return "blueprints." + commandString;
    }

}
