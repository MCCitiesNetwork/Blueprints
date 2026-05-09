package io.github.bl3rune.blueprints.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Owns the interaction cooldown timestamps and per-player block ignore lists
 * previously stored as static maps on {@code PlayerInteractListener}. Phase 3
 * of the architecture overhaul moves them here so they are no longer
 * mutable static state and can be injected into listeners.
 */
public final class InteractionCooldownService {

    private final Map<String, Long> lastInteractionPerPlayer = new HashMap<>();
    private final Map<String, List<String>> blockIgnoreListPerPlayer = new HashMap<>();

    public boolean isOnCooldown(String playerUUID, long cooldownMillis, long now) {
        long last = lastInteractionPerPlayer.getOrDefault(playerUUID, now - cooldownMillis);
        return last + cooldownMillis > now;
    }

    public void markInteraction(String playerUUID, long now) {
        lastInteractionPerPlayer.put(playerUUID, now);
    }

    public List<String> getIgnoreList(String playerUUID) {
        return blockIgnoreListPerPlayer.getOrDefault(playerUUID, new ArrayList<>());
    }

    public boolean toggleIgnore(String playerUUID, String locationString) {
        List<String> ignoreList = blockIgnoreListPerPlayer.getOrDefault(playerUUID, new ArrayList<>());
        boolean wasPresent = ignoreList.stream().anyMatch(l -> l.equals(locationString));
        if (wasPresent) {
            ignoreList = ignoreList.stream()
                    .filter(l -> !l.equals(locationString))
                    .collect(Collectors.toList());
        } else {
            ignoreList.add(locationString);
        }
        blockIgnoreListPerPlayer.put(playerUUID, ignoreList);
        return !wasPresent;
    }

    public boolean hasIgnoreEntries(String playerUUID) {
        return blockIgnoreListPerPlayer.containsKey(playerUUID);
    }

    public void clearIgnoreList(String playerUUID) {
        blockIgnoreListPerPlayer.remove(playerUUID);
    }
}
