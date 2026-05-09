package io.github.bl3rune.blueprints.config;

import java.util.ArrayList;
import java.util.List;

public class PlayerConfig {

    private List<String> ignoredMaterials = new ArrayList<>(); // Materials to ignore

    public List<String> getIgnoredMaterials() {
        return ignoredMaterials;
    }

    public void setIgnoredMaterials(List<String> ignoredMaterials) {
        this.ignoredMaterials = ignoredMaterials;
    }
}
