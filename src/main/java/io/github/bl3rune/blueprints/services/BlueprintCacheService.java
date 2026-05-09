package io.github.bl3rune.blueprints.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.data.ImportedBlu3printData;
import io.github.bl3rune.blueprints.items.Blu3printItem;
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
    private final Map<String, Blu3printData> cache = new HashMap<>();

    public BlueprintCacheService(BlueprintRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }

    public void loadFromDisk() {
        List<BlueprintRecord> records = repository.loadAll();
        if (records.isEmpty()) {
            return;
        }
        Map<String, Blu3printData> loaded = new HashMap<>();
        for (BlueprintRecord record : records) {
            loaded.put(record.getUuid(),
                    new ImportedBlu3printData(null, record.getEncoded(), record.getUuid()));
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

    public Blu3printData get(String key) {
        return cache.getOrDefault(key, null);
    }

    public Blu3printData get(ItemStack blu3print, Player player) {
        String key = Blu3printItem.extractCacheKeyFromBlu3print(blu3print);
        if (key == null) {
            logger.warning("Blu3print ID is missing from the server cache");
            if (player != null) {
                player.sendMessage("Blu3print ID is missing from the server cache");
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

    public synchronized void saveOrUpdate(String key, Blu3printData data) {
        cache.put(key, data);
        persist();
    }
}
