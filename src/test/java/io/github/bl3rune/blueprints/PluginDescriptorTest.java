package io.github.bl3rune.blueprints;

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
 * Locks the Bukkit-visible plugin descriptor surface after the Phase 1 rename.
 *
 * <p>The plugin's canonical identity is now {@code Blueprints}. The legacy
 * {@code blu3print.*} command names live on as command aliases, and every
 * legacy permission stays declared with the new permission as its child so
 * existing operator grants keep working through the migration window.
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
        assertEquals("Blueprints", yml.get("name"));
        assertEquals("Blueprints", yml.get("prefix"));
        assertEquals("io.github.bl3rune.blueprints.Blueprints", yml.get("main"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void allExpectedCommandsRegistered() {
        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> commands = (Map<String, Object>) yml.get("commands");
        assertNotNull(commands);

        Set<String> expected = new HashSet<>(Arrays.asList(
                "blueprints",
                "blueprints.duplicate",
                "blueprints.rotate",
                "blueprints.turn",
                "blueprints.face",
                "blueprints.import",
                "blueprints.export",
                "blueprints.scale",
                "blueprints.name",
                "blueprints.give",
                "blueprints.help",
                "blueprints.config",
                "blueprints.player-config",
                "blueprints.global-config"));
        assertTrue(commands.keySet().containsAll(expected),
                "missing commands: expected " + expected + " but got " + commands.keySet());
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyCommandKeepsItsLegacyBlu3printAlias() {
        // Migration: every new blueprints.<sub> command must list the legacy
        // blu3print.<sub> form (or its shortened variant) as an alias so user
        // scripts keep resolving until the migration window ends.
        Map<String, String> expectedLegacyAlias = new LinkedHashMap<>();
        expectedLegacyAlias.put("blueprints", "blu3print");
        expectedLegacyAlias.put("blueprints.duplicate", "blu3print.duplicate");
        expectedLegacyAlias.put("blueprints.rotate", "blu3print.rotate");
        expectedLegacyAlias.put("blueprints.turn", "blu3print.turn");
        expectedLegacyAlias.put("blueprints.face", "blu3print.face");
        expectedLegacyAlias.put("blueprints.import", "blu3print.import");
        expectedLegacyAlias.put("blueprints.export", "blu3print.export");
        expectedLegacyAlias.put("blueprints.scale", "blu3print.scale");
        expectedLegacyAlias.put("blueprints.name", "blu3print.name");
        expectedLegacyAlias.put("blueprints.give", "blu3print.give");
        expectedLegacyAlias.put("blueprints.help", "blu3print.help");
        expectedLegacyAlias.put("blueprints.config", "blu3print.config");
        expectedLegacyAlias.put("blueprints.player-config", "blu3print.player-config");
        expectedLegacyAlias.put("blueprints.global-config", "blu3print.global-config");

        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> commands = (Map<String, Object>) yml.get("commands");

        for (Map.Entry<String, String> e : expectedLegacyAlias.entrySet()) {
            Map<String, Object> def = (Map<String, Object>) commands.get(e.getKey());
            assertNotNull(def, "command missing: " + e.getKey());
            List<String> aliases = (List<String>) def.get("aliases");
            assertNotNull(aliases, e.getKey() + " has no aliases");
            assertTrue(aliases.contains(e.getValue()),
                    e.getKey() + " missing legacy alias " + e.getValue() + " (got " + aliases + ")");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void everyCommandKeepsItsShortBlu3Alias() {
        // The terse blu3.<sub> shortcut is also part of the historical
        // surface; it must keep resolving for every command.
        Map<String, String> expectedShort = new LinkedHashMap<>();
        expectedShort.put("blueprints", "blu3");
        expectedShort.put("blueprints.duplicate", "blu3.duplicate");
        expectedShort.put("blueprints.rotate", "blu3.rotate");
        expectedShort.put("blueprints.turn", "blu3.turn");
        expectedShort.put("blueprints.face", "blu3.face");
        expectedShort.put("blueprints.import", "blu3.import");
        expectedShort.put("blueprints.export", "blu3.export");
        expectedShort.put("blueprints.scale", "blu3.scale");
        expectedShort.put("blueprints.name", "blu3.name");
        expectedShort.put("blueprints.give", "blu3.give");
        expectedShort.put("blueprints.help", "blu3.help");
        expectedShort.put("blueprints.config", "blu3.config");
        expectedShort.put("blueprints.player-config", "blu3.player");
        expectedShort.put("blueprints.global-config", "blu3.global");

        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> commands = (Map<String, Object>) yml.get("commands");

        for (Map.Entry<String, String> e : expectedShort.entrySet()) {
            Map<String, Object> def = (Map<String, Object>) commands.get(e.getKey());
            assertNotNull(def, "command missing: " + e.getKey());
            List<String> aliases = (List<String>) def.get("aliases");
            assertTrue(aliases.contains(e.getValue()),
                    e.getKey() + " missing short alias " + e.getValue() + " (got " + aliases + ")");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void permissionNamespaceLocked() {
        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> perms = (Map<String, Object>) yml.get("permissions");
        assertNotNull(perms);

        assertTrue(perms.containsKey("blueprints.*"));
        assertTrue(perms.containsKey("blueprints.basics"));
        Map<String, Object> basics = (Map<String, Object>) perms.get("blueprints.basics");
        assertEquals("not op", basics.get("default"));

        Map<String, Object> updateMsg = (Map<String, Object>) perms.get("blueprints.update-available-message");
        assertEquals("op", updateMsg.get("default"));

        Map<String, Object> noBlockCost = (Map<String, Object>) perms.get("blueprints.no-block-cost");
        assertEquals(false, noBlockCost.get("default"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void legacyPermissionsStillDeclaredAndGrantNewOnes() {
        // Migration shim: granting a legacy blu3print.<sub> perm must still
        // grant access to the new blueprints.<sub> perm via the children map.
        Map<String, Object> yml = loadPluginYml();
        Map<String, Object> perms = (Map<String, Object>) yml.get("permissions");

        for (String legacy : new String[] {
                "blu3print.*", "blu3print.basics", "blu3print",
                "blu3print.duplicate", "blu3print.rotate", "blu3print.turn",
                "blu3print.face", "blu3print.import", "blu3print.export",
                "blu3print.scale", "blu3print.name", "blu3print.give",
                "blu3print.help", "blu3print.config",
                "blu3print.player-config", "blu3print.global-config",
                "blu3print.holograms", "blu3print.no-size-limit",
                "blu3print.no-scale-limit", "blu3print.force-place-discount",
                "blu3print.no-block-cost", "blu3print.update-available-message" }) {
            Map<String, Object> def = (Map<String, Object>) perms.get(legacy);
            assertNotNull(def, "legacy permission missing: " + legacy);
            Map<String, Object> children = (Map<String, Object>) def.get("children");
            assertNotNull(children, legacy + " has no children mapping the new permission");
            String newPerm = "blueprints" + legacy.substring("blu3print".length());
            assertEquals(Boolean.TRUE, children.get(newPerm),
                    legacy + " should grant " + newPerm + " via children");
        }
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
