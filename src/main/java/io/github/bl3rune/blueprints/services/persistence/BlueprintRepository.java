package io.github.bl3rune.blueprints.services.persistence;

import java.util.List;

/**
 * Storage adapter for the blueprint cache. Phase 3 hides the JSON-on-disk
 * detail behind this interface so later phases can swap implementations
 * (e.g. an SQL-backed adapter) without touching the cache service.
 */
public interface BlueprintRepository {

    List<BlueprintRecord> loadAll();

    void saveAll(List<BlueprintRecord> records);
}
