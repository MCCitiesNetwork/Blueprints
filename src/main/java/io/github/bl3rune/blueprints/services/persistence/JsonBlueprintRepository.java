package io.github.bl3rune.blueprints.services.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;

/**
 * JSON-backed implementation of {@link BlueprintRepository}. Reads from
 * {@code blueprints.json} and falls back to the legacy {@code blu3prints.json}
 * filename inside the same data folder for the migration window.
 */
public final class JsonBlueprintRepository implements BlueprintRepository {

    private static final String CURRENT_FILE = "blueprints.json";
    private static final String LEGACY_FILE = "blu3prints.json";

    private final File dataFolder;
    private final Logger logger;
    private final Gson gson = new Gson();

    public JsonBlueprintRepository(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BlueprintRecord> loadAll() {
        try {
            File file = new File(dataFolder.getAbsolutePath() + File.separator + CURRENT_FILE);
            if (!file.exists()) {
                File legacy = new File(dataFolder.getAbsolutePath() + File.separator + LEGACY_FILE);
                if (legacy.exists()) {
                    file = legacy;
                } else {
                    return new ArrayList<>();
                }
            }
            try (FileReader reader = new FileReader(file)) {
                Map<String, String> entries = gson.fromJson(reader, Map.class);
                if (entries == null || entries.isEmpty()) {
                    logger.warning("No saved blu3prints found in blu3prints.json");
                    return new ArrayList<>();
                }
                List<BlueprintRecord> records = new ArrayList<>(entries.size());
                for (Map.Entry<String, String> e : entries.entrySet()) {
                    records.add(new BlueprintRecord(e.getKey(), e.getValue()));
                }
                logger.warning("Loaded Cached blu3prints from blu3prints.json");
                return records;
            }
        } catch (Exception e) {
            logger.severe("Failed to load blu3prints to cache : " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public synchronized void saveAll(List<BlueprintRecord> records) {
        try {
            File file = new File(dataFolder.getAbsolutePath() + File.separator + CURRENT_FILE);
            file.getParentFile().mkdir();
            file.createNewFile();
            Map<String, String> payload = new LinkedHashMap<>();
            for (BlueprintRecord record : records) {
                payload.put(record.getUuid(), record.getEncoded());
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(payload, writer);
                writer.flush();
            }
            logger.warning("Cached blu3prints saved to blu3prints.json");
        } catch (Exception e) {
            logger.severe("Failed to save cached blu3prints : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
