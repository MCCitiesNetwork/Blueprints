package io.github.bl3rune.blueprints.config;

import java.util.ArrayList;
import java.util.List;

public class PlayerBlueprintConfig {

    private String uuid; // Blu3print to apply config to
    private int [] [] hologramViewLayers; // X[], Y[], Z[]
    private List<String> ignoredMaterials; // Materials to ignore


    public PlayerBlueprintConfig(String uuid) {
        this.uuid = uuid;
        this.hologramViewLayers = null;
        this.ignoredMaterials = new ArrayList<>();
    }

    public boolean uuidMatches(String uuid) {
        if (this.uuid == null) return false;
        return this.uuid.equals(uuid);
    }

    public int [] [] getHologramViewLayers() {
        if (hologramViewLayers == null) {
            return new int [][] {
                null,
                null,
                null
            };
        }
        return hologramViewLayers;
    }

    public void setHologramViewLayers(int [][] layers) {
        this.hologramViewLayers = layers;
    }

    public List<String> getIgnoredMaterials() {
        if (ignoredMaterials == null) {
            return new ArrayList<>();
        }
        return ignoredMaterials;
    }

    public void setIgnoredMaterials(List<String> materials) {
        this.ignoredMaterials = materials;
    }

}
