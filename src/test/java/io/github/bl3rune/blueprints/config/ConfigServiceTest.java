package io.github.bl3rune.blueprints.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.jupiter.api.Test;

import io.github.bl3rune.blueprints.enums.Alignment;
import io.github.bl3rune.blueprints.enums.GConfig;
import io.github.bl3rune.blueprints.enums.SemanticLevel;

/**
 * Phase 8 coverage for the typed config service. The previous implementation
 * silently swallowed parse failures via a broad try/catch — these tests pin
 * the new contract: integers that fail to parse fall back to {@code null}
 * (so the {@link ConfigSnapshot} default is used by callers), the legacy
 * {@code blu3print.*} root is honored only when the new key is absent, and
 * boolean/enum defaults are explicit.
 *
 * <p>Values are written directly onto a {@link MemoryConfiguration} rather
 * than parsed from YAML, because Bukkit's {@code YamlConfiguration} on the
 * test classpath is compiled against SnakeYAML 1.x while the project pulls
 * in SnakeYAML 2.x for {@code PluginDescriptorTest}.
 */
class ConfigServiceTest {

    @Test
    void readsAllFieldsFromCanonicalBlueprintsRoot() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());
        for (GConfig entry : GConfig.values()) {
            cfg.set(entry.getConfigPath(), null);
        }
        cfg.set(GConfig.MAX_SIZE.getConfigPath(), "10");
        cfg.set(GConfig.MAX_SCALE.getConfigPath(), "4");
        cfg.set(GConfig.MAX_OVERALL_SIZE.getConfigPath(), "256");
        cfg.set(GConfig.COOLDOWN.getConfigPath(), "5");
        cfg.set(GConfig.HOLOGRAM_TTL.getConfigPath(), "30");
        cfg.set(GConfig.UPDATE_CHECK_INTERVAL.getConfigPath(), "12");
        cfg.set("blueprints.ignored-materials", Arrays.asList("AIR", "WATER"));
        cfg.set(GConfig.ALIGNMENT.getConfigPath(), "CENTER");
        cfg.set(GConfig.RELATIVE.getConfigPath(), true);
        cfg.set(GConfig.FORCE_PLACE_PENALTY.getConfigPath(), false);
        cfg.set(GConfig.FREE_PLACEMENT_MESSAGE.getConfigPath(), true);
        cfg.set(GConfig.FORCED_PLACEMENT_MESSAGE.getConfigPath(), false);
        cfg.set(GConfig.DISCOUNT_PLACEMENT_MESSAGE.getConfigPath(), true);
        cfg.set(GConfig.UPDATE_AVAILABLE_MESSAGE.getConfigPath(), false);
        cfg.set(GConfig.COOLDOWN_MESSAGE.getConfigPath(), true);
        cfg.set(GConfig.VERBOSE_LOGGING.getConfigPath(), true);
        cfg.set(GConfig.IMPORTED_BLU3PRINTS_LOGGING.getConfigPath(), true);
        cfg.set(GConfig.UPDATE_LEVEL.getConfigPath(), "MAJOR");

        ConfigSnapshot snap = refresh(cfg);

        assertEquals(10, snap.maxSize);
        assertEquals(4, snap.maxScale);
        assertEquals(256, snap.maxOverallSize);
        assertEquals(5, snap.cooldown);
        assertEquals(30, snap.hologramTtl);
        assertEquals(12, snap.updateCheckInterval);
        assertEquals(Arrays.asList("AIR", "WATER"), snap.ignoredMaterials);
        assertEquals(Alignment.CENTER, snap.alignment);
        assertTrue(snap.relativePlacement);
        assertFalse(snap.forcePlacePenalty);
        assertTrue(snap.freePlacementMessageEnabled);
        assertFalse(snap.forcePlacementMessageEnabled);
        assertTrue(snap.discountPlacementMessageEnabled);
        assertFalse(snap.updateAvailableMessageEnabled);
        assertTrue(snap.cooldownMessageEnabled);
        assertTrue(snap.verboseLogging);
        assertTrue(snap.importedBlu3printsLoggingEnabled);
        assertEquals(SemanticLevel.MAJOR, snap.updateLoggingLevel);
    }

    @Test
    void fallsBackToLegacyBlu3printRootWhenNewKeyAbsent() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());
        cfg.set(GConfig.MAX_SIZE.getLegacyConfigPath(), "7");
        cfg.set(GConfig.COOLDOWN.getLegacyConfigPath(), "3");
        cfg.set("blu3print.ignored-materials", Arrays.asList("LAVA"));
        cfg.set(GConfig.ALIGNMENT.getLegacyConfigPath(), "LEFT");

        ConfigSnapshot snap = refresh(cfg);

        assertEquals(7, snap.maxSize);
        assertEquals(3, snap.cooldown);
        assertEquals(Arrays.asList("LAVA"), snap.ignoredMaterials);
        assertEquals(Alignment.LEFT, snap.alignment);
    }

    @Test
    void preferNewKeyOverLegacyKeyWhenBothPresent() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());
        cfg.set(GConfig.MAX_SIZE.getConfigPath(), "10");
        cfg.set(GConfig.MAX_SIZE.getLegacyConfigPath(), "99");

        ConfigSnapshot snap = refresh(cfg);

        assertEquals(10, snap.maxSize, "new blueprints.* root takes priority over legacy blu3print.*");
    }

    @Test
    void invalidIntegerFallsBackToNull() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());
        cfg.set(GConfig.MAX_SIZE.getConfigPath(), "not-a-number");
        cfg.set(GConfig.COOLDOWN.getConfigPath(), "5");

        ConfigSnapshot snap = refresh(cfg);

        assertNull(snap.maxSize, "unparseable integer must surface as null so callers apply their default");
        assertEquals(5, snap.cooldown, "other fields keep parsing past a malformed sibling");
    }

    @Test
    void missingIntegerYieldsNull() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());
        cfg.set(GConfig.COOLDOWN.getConfigPath(), "5");

        ConfigSnapshot snap = refresh(cfg);

        assertNull(snap.maxSize);
        assertNull(snap.maxScale);
        assertNull(snap.maxOverallSize);
        assertEquals(5, snap.cooldown);
    }

    @Test
    void missingBooleanUsesExplicitFalseDefault() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());
        cfg.set(GConfig.MAX_SIZE.getConfigPath(), "10");

        ConfigSnapshot snap = refresh(cfg);

        assertFalse(snap.relativePlacement);
        assertFalse(snap.forcePlacePenalty);
        assertFalse(snap.verboseLogging);
        assertFalse(snap.freePlacementMessageEnabled);
    }

    @Test
    void invalidEnumFallsBackToNull() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());
        cfg.set(GConfig.ALIGNMENT.getConfigPath(), "NONSENSE_VALUE");

        ConfigSnapshot snap = refresh(cfg);

        assertNull(snap.alignment, "unknown enum constant must surface as null, not throw");
    }

    @Test
    void missingIgnoredMaterialsDefaultsToAir() {
        FileConfiguration cfg = wrap(new MemoryConfiguration());

        ConfigSnapshot snap = refresh(cfg);

        assertEquals(Arrays.asList("AIR"), snap.ignoredMaterials);
    }

    @Test
    void refreshReplacesSnapshotAtomically() {
        ConfigService service = new ConfigService(Logger.getLogger("test"));

        FileConfiguration first = wrap(new MemoryConfiguration());
        first.set(GConfig.MAX_SIZE.getConfigPath(), "1");
        service.refresh(first);
        assertEquals(1, service.current().maxSize);

        FileConfiguration second = wrap(new MemoryConfiguration());
        second.set(GConfig.MAX_SIZE.getConfigPath(), "2");
        service.refresh(second);
        assertEquals(2, service.current().maxSize);
    }

    private ConfigSnapshot refresh(FileConfiguration cfg) {
        ConfigService service = new ConfigService(Logger.getLogger("test"));
        service.refresh(cfg);
        return service.current();
    }

    /**
     * The new {@link ConfigService#refresh(FileConfiguration)} overload takes
     * a {@link FileConfiguration} directly. {@link MemoryConfiguration} is a
     * {@code Configuration} but not a {@code FileConfiguration}, so wrap it
     * in a minimal {@link FileConfiguration} subclass that delegates reads.
     */
    private static FileConfiguration wrap(MemoryConfiguration backing) {
        return new FileConfiguration() {
            { addDefaults(backing.getDefaults() == null ? new MemoryConfiguration() : backing.getDefaults()); }

            @Override
            public boolean contains(String path) {
                return backing.contains(path);
            }

            @Override
            public Object get(String path, Object def) {
                return backing.get(path, def);
            }

            @Override
            public String getString(String path, String def) {
                return backing.getString(path, def);
            }

            @Override
            public boolean getBoolean(String path, boolean def) {
                return backing.getBoolean(path, def);
            }

            @Override
            public java.util.List<String> getStringList(String path) {
                return backing.getStringList(path);
            }

            @Override
            public void set(String path, Object value) {
                backing.set(path, value);
            }

            @Override
            public String saveToString() {
                return "";
            }

            @Override
            public void loadFromString(String contents) {
                // unused
            }

            @Override
            protected String buildHeader() {
                return "";
            }
        };
    }
}
