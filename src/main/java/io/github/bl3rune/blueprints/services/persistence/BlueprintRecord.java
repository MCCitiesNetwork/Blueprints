package io.github.bl3rune.blueprints.services.persistence;

/**
 * Typed payload for one persisted blueprint entry. Phase 3 replaces the
 * raw {@code Map<String, String>} that used to be parsed inline in the
 * plugin main with this value type so the on-disk shape and the in-memory
 * shape are explicit.
 */
public final class BlueprintRecord {

    private final String uuid;
    private final String encoded;

    public BlueprintRecord(String uuid, String encoded) {
        if (uuid == null || encoded == null) {
            throw new IllegalArgumentException("uuid and encoded must not be null");
        }
        this.uuid = uuid;
        this.encoded = encoded;
    }

    public String getUuid() {
        return uuid;
    }

    public String getEncoded() {
        return encoded;
    }
}
