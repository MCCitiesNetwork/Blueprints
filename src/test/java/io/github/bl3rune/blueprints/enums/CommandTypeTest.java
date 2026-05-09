package io.github.bl3rune.blueprints.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Phase 0 lockdown for CommandType. Snapshots the command-string and
 * full-command-name for every entry, plus which entries advertise tab
 * completers. Phase 5 will rewrite this enum into DI registration; this
 * test is the contract the new wiring must satisfy.
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
            assertNotNull(c.getCommandExecutor(), c.name() + " missing executor");
        }
        assertEquals(14, CommandType.values().length);
    }

    @Test
    void tabCompletersOnlyOnExpectedCommands() {
        // These commands ship a tab completer; the others must not.
        for (CommandType c : new CommandType[] {
                CommandType.FACE, CommandType.ROTATE, CommandType.TURN,
                CommandType.SCALE, CommandType.GIVE, CommandType.CONFIG,
                CommandType.PLAYER, CommandType.GLOBAL }) {
            assertNotNull(c.getTabCompleter(), c.name() + " expected tab completer");
        }
        for (CommandType c : new CommandType[] {
                CommandType.BLU3PRINT, CommandType.DUPLICATE, CommandType.IMPORT,
                CommandType.EXPORT, CommandType.NAME, CommandType.HELP }) {
            assertNull(c.getTabCompleter(), c.name() + " should not have tab completer");
        }
    }
}
