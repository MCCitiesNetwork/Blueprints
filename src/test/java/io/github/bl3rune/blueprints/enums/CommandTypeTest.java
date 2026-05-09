package io.github.bl3rune.blueprints.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Locks the public CommandType contract: command strings, full command
 * names, and which entries advertise a tab completer. After Phase 5 the
 * enum is pure metadata; executor instances are built in PluginBootstrap
 * and held in CommandRegistry, so this test no longer asserts the enum
 * carries those instances - only the metadata other code keys off of.
 */
class CommandTypeTest {

    @Test
    void allCommandStringsAndFullNamesLocked() {
        Map<CommandType, String> expectedString = new LinkedHashMap<>();
        Map<CommandType, String> expectedFull = new LinkedHashMap<>();

        expectedString.put(CommandType.BLU3PRINT, "blueprints");
        expectedFull.put(CommandType.BLU3PRINT, "blueprints");

        expectedString.put(CommandType.DUPLICATE, "duplicate");
        expectedFull.put(CommandType.DUPLICATE, "blueprints.duplicate");

        expectedString.put(CommandType.FACE, "face");
        expectedFull.put(CommandType.FACE, "blueprints.face");

        expectedString.put(CommandType.ROTATE, "rotate");
        expectedFull.put(CommandType.ROTATE, "blueprints.rotate");

        expectedString.put(CommandType.TURN, "turn");
        expectedFull.put(CommandType.TURN, "blueprints.turn");

        expectedString.put(CommandType.IMPORT, "import");
        expectedFull.put(CommandType.IMPORT, "blueprints.import");

        expectedString.put(CommandType.EXPORT, "export");
        expectedFull.put(CommandType.EXPORT, "blueprints.export");

        expectedString.put(CommandType.SCALE, "scale");
        expectedFull.put(CommandType.SCALE, "blueprints.scale");

        expectedString.put(CommandType.NAME, "name");
        expectedFull.put(CommandType.NAME, "blueprints.name");

        expectedString.put(CommandType.GIVE, "give");
        expectedFull.put(CommandType.GIVE, "blueprints.give");

        expectedString.put(CommandType.HELP, "help");
        expectedFull.put(CommandType.HELP, "blueprints.help");

        expectedString.put(CommandType.CONFIG, "config");
        expectedFull.put(CommandType.CONFIG, "blueprints.config");

        expectedString.put(CommandType.PLAYER, "player-config");
        expectedFull.put(CommandType.PLAYER, "blueprints.player-config");

        expectedString.put(CommandType.GLOBAL, "global-config");
        expectedFull.put(CommandType.GLOBAL, "blueprints.global-config");

        for (CommandType c : CommandType.values()) {
            assertEquals(expectedString.get(c), c.toString(), "toString for " + c.name());
            assertEquals(expectedFull.get(c), c.getFullCommandName(), "fullCommandName for " + c.name());
        }
        assertEquals(14, CommandType.values().length);
    }

    @Test
    void tabCompletersOnlyOnExpectedCommands() {
        for (CommandType c : new CommandType[] {
                CommandType.FACE, CommandType.ROTATE, CommandType.TURN,
                CommandType.SCALE, CommandType.GIVE, CommandType.CONFIG,
                CommandType.PLAYER, CommandType.GLOBAL }) {
            assertTrue(c.hasTabCompleter(), c.name() + " expected tab completer");
        }
        for (CommandType c : new CommandType[] {
                CommandType.BLU3PRINT, CommandType.DUPLICATE, CommandType.IMPORT,
                CommandType.EXPORT, CommandType.NAME, CommandType.HELP }) {
            assertFalse(c.hasTabCompleter(), c.name() + " should not have tab completer");
        }
    }
}
