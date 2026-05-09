package io.github.bl3rune.blueprints.services;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Phase 1 migration shim wrapped as a service. When the plugin descriptor
 * name flipped from {@code Blu3PrintPlugin} to {@code Blueprints}, Bukkit
 * began using a new data folder. This service copies the cached blueprints
 * file and {@code config.yml} from the legacy folder into the new one when
 * the new folder is empty.
 *
 * <p>The legacy folder is left intact as a safety net. Removal of this
 * service ends the migration window.
 */
public final class DataFolderMigrator {

    private static final String LEGACY_FOLDER_NAME = "Blu3PrintPlugin";
    private static final String LEGACY_DATA_FILE = "blu3prints.json";
    private static final String NEW_DATA_FILE = "blueprints.json";
    private static final String CONFIG_FILE = "config.yml";

    public void migrateIfNeeded(JavaPlugin plugin) {
        Logger logger = plugin.getLogger();
        try {
            File newFolder = plugin.getDataFolder();
            File legacyFolder = new File(newFolder.getParentFile(), LEGACY_FOLDER_NAME);
            if (!legacyFolder.isDirectory()) {
                return;
            }
            if (!newFolder.isDirectory()) {
                newFolder.mkdirs();
            }

            File legacyData = new File(legacyFolder, LEGACY_DATA_FILE);
            File newData = new File(newFolder, NEW_DATA_FILE);
            if (legacyData.isFile() && !newData.isFile()
                    && !new File(newFolder, LEGACY_DATA_FILE).isFile()) {
                Files.copy(legacyData.toPath(), newData.toPath());
                logger.info("Migrated cached blueprints from legacy "
                        + legacyData.getAbsolutePath() + " to " + newData.getAbsolutePath());
            }

            File legacyConfig = new File(legacyFolder, CONFIG_FILE);
            File newConfig = new File(newFolder, CONFIG_FILE);
            if (legacyConfig.isFile() && !newConfig.isFile()) {
                Files.copy(legacyConfig.toPath(), newConfig.toPath());
                logger.info("Migrated legacy config.yml from "
                        + legacyConfig.getAbsolutePath() + " to " + newConfig.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.warning("Failed to migrate legacy data folder: " + e.getMessage());
        }
    }
}
