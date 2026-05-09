package io.github.bl3rune.blueprints.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.data.BlueprintData;
import io.github.bl3rune.blueprints.data.ImportedBlueprintData;
import io.github.bl3rune.blueprints.items.BlueprintItem;
import io.github.bl3rune.blueprints.services.persistence.BlueprintRecord;
import io.github.bl3rune.blueprints.services.persistence.BlueprintRepository;

/**
 * Owns the in-memory blueprint cache previously held on the plugin main
 * class. Reads and writes are funnelled through a {@link BlueprintRepository}
 * so the storage adapter can be swapped without touching consumers.
 */
public final class BlueprintCacheService {

    private final BlueprintRepository repository;
    private final Logger logger;
    private final Map<String, BlueprintData> cache = new HashMap<>();

    public BlueprintCacheService(BlueprintRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }

    public void loadFromDisk() {
        List<BlueprintRecord> records = repository.loadAll();
        if (records.isEmpty()) {
            return;
        }
        Map<String, BlueprintData> loaded = new HashMap<>();
        for (BlueprintRecord record : records) {
            loaded.put(record.getUuid(),
                    new ImportedBlueprintData(null, record.getEncoded(), record.getUuid()));
        }
        if (GlobalConfig.isImportedBlu3printsLoggingEnabled()) {
            loaded.forEach((k, v) -> logger.info(k + " : " + v.getEncodedString()));
        }
        synchronized (cache) {
            cache.clear();
            cache.putAll(loaded);
        }
    }

    public synchronized void persist() {
        List<BlueprintRecord> records = new ArrayList<>(cache.size());
        cache.forEach((k, v) -> records.add(new BlueprintRecord(k, v.getEncodedString())));
        repository.saveAll(records);
    }

    public BlueprintData get(String key) {
        return cache.getOrDefault(key, null);
    }

    public BlueprintData get(ItemStack blu3print, Player player) {
        String key = BlueprintItem.extractCacheKeyFromBlu3print(blu3print);
        if (key == null) {
            logger.warning("Blueprint ID is missing from the server cache");
            if (player != null) {
                player.sendMessage("Blueprint ID is missing from the server cache");
            }
        }
        return get(key);
    }

    public String keyFromEncoding(String encoded) {
        return cache.entrySet().stream()
                .filter(e -> e.getValue().getEncodedString().equals(encoded))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public synchronized void saveOrUpdate(String key, BlueprintData data) {
        cache.put(key, data);
        persist();
    }
}
