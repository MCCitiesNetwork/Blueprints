package io.github.bl3rune.blueprints.enums;

public enum Config {
    HOLOGRAM_VIEW_XYZ,
    HOLOGRAM_VIEW_X,
    HOLOGRAM_VIEW_Y,
    HOLOGRAM_VIEW_Z,
    IGNORE_MATERIAL(true),
    ALLOW_MATERIAL(true),
    CLEAR(true);

    private boolean playerLevelConfig = false;

    Config() {
        this(false);
    }

    Config(boolean playerLevelConfig) {
        this.playerLevelConfig = playerLevelConfig;
    }

    public void setPlayerLevelConfig(boolean levelConfig) {
        this.playerLevelConfig = levelConfig;
    }

    public boolean isPlayerLevelConfig() {
        return playerLevelConfig;
    }



}
