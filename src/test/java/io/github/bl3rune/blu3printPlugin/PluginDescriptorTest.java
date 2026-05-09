package io.github.bl3rune.blu3printPlugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Phase 0 behavior lockdown for plugin.yml.
 *
 * <p>This test snapshots the current Bukkit-visible surface (plugin name,
 * main class, command names + aliases, permission namespace) so the rename
 * to {@code Blueprints} cannot silently drop or rename anything users have
 * scripted against. The Phase 1 rename PR is expected to update these
 * expectations in lockstep with adding migration aliases for the old
 * {@code blu3print} forms.
 */
class PluginDescriptorTest {

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadPluginYml() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(is, "plugin.yml not found on test classpath");
            return new Yaml().load(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void pluginIdentity() {
        Map<String, Object> yml = loadPluginYml();
        assertEquals("Blu3PrintPlugin", yml.get("name"));
        assertEquals("Blu3Print", yml.get("prefix"));
        assertEquals("io.github.bl3rune.blu3printPlugin.Blu3PrintPlugin", yml.get("main"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void allExpectedCommandsRegistered() {
        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> commands = (Map<String, Object>) yml.get("commands");
        assertNotNull(commands);

        // The complete current command surface. Phase 1 will add new
        // "blueprints.*" entries alongside these for the migration window.
        Set<String> expected = new HashSet<>(Arrays.asList(
                "blu3print",
                "blu3print.duplicate",
                "blu3print.rotate",
                "blu3print.turn",
                "blu3print.face",
                "blu3print.import",
                "blu3print.export",
                "blu3print.scale",
                "blu3print.name",
                "blu3print.give",
                "blu3print.help",
                "blu3print.config",
                "blu3print.player-config",
                "blu3print.global-config"));
        assertTrue(commands.keySet().containsAll(expected),
                "missing commands: expected " + expected + " but got " + commands.keySet());
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyCommandHasItsCurrentBlu3Alias() {
        // Snapshot of the exact alias each command exposes today. Most are
        // mechanical "blu3print." -> "blu3." but the two -config commands
        // use shortened aliases (blu3.player, blu3.global). Phase 1 keeps
        // every alias listed here as part of the migration window.
        Map<String, String> expectedAlias = new LinkedHashMap<>();
        expectedAlias.put("blu3print", "blu3");
        expectedAlias.put("blu3print.duplicate", "blu3.duplicate");
        expectedAlias.put("blu3print.rotate", "blu3.rotate");
        expectedAlias.put("blu3print.turn", "blu3.turn");
        expectedAlias.put("blu3print.face", "blu3.face");
        expectedAlias.put("blu3print.import", "blu3.import");
        expectedAlias.put("blu3print.export", "blu3.export");
        expectedAlias.put("blu3print.scale", "blu3.scale");
        expectedAlias.put("blu3print.name", "blu3.name");
        expectedAlias.put("blu3print.give", "blu3.give");
        expectedAlias.put("blu3print.help", "blu3.help");
        expectedAlias.put("blu3print.config", "blu3.config");
        expectedAlias.put("blu3print.player-config", "blu3.player");
        expectedAlias.put("blu3print.global-config", "blu3.global");

        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> commands = (Map<String, Object>) yml.get("commands");

        for (Map.Entry<String, String> e : expectedAlias.entrySet()) {
            Map<String, Object> def = (Map<String, Object>) commands.get(e.getKey());
            assertNotNull(def, "command missing: " + e.getKey());
            List<String> aliases = (List<String>) def.get("aliases");
            assertNotNull(aliases, e.getKey() + " has no aliases");
            assertTrue(aliases.contains(e.getValue()),
                    e.getKey() + " missing alias " + e.getValue() + " (got " + aliases + ")");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void permissionNamespaceLocked() {
        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> perms = (Map<String, Object>) yml.get("permissions");
        assertNotNull(perms);

        // Locking the umbrella permissions and a few defaults.
        assertTrue(perms.containsKey("blu3print.*"));
        assertTrue(perms.containsKey("blu3print.basics"));
        Map<String, Object> basics = (Map<String, Object>) perms.get("blu3print.basics");
        assertEquals("not op", basics.get("default"));

        Map<String, Object> updateMsg = (Map<String, Object>) perms.get("blu3print.update-available-message");
        assertEquals("op", updateMsg.get("default"));

        Map<String, Object> noBlockCost = (Map<String, Object>) perms.get("blu3print.no-block-cost");
        assertEquals(false, noBlockCost.get("default"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyCommandReferencesAMatchingPermission() {
        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> commands = (Map<String, Object>) yml.get("commands");
        Map<String, Object> perms = (Map<String, Object>) yml.get("permissions");

        for (Map.Entry<String, Object> entry : commands.entrySet()) {
            Map<String, Object> def = (Map<String, Object>) entry.getValue();
            String permission = (String) def.get("permission");
            assertNotNull(permission, entry.getKey() + " has no permission set");
            assertTrue(perms.containsKey(permission),
                    entry.getKey() + " references undefined permission " + permission);
        }
    }
}
