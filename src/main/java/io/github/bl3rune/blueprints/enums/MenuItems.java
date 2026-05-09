package io.github.bl3rune.blueprints.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum MenuItems {

    DUPLICATE("Duplicate", "Duplicate a blu3print", Material.PURPUR_BLOCK, 2),
    FACE("Change Side Facing", "Change the side facing you of a blu3print", Material.DISPENSER),
    ROTATE("Rotate", "Rotate a blu3print", Material.COMPASS),
    TURN("Turn", "Turn a blu3print", Material.PURPUR_STAIRS),
    EXPORT("Export", "Export a blu3print", Material.CHEST_MINECART),
    SCALE("Change Scale", "Double the size of a blu3print, or set specified scale", Material.PURPUR_PILLAR, 2),
    GIVE("Give", "Give a blu3print writer", Material.CHEST),
    HELP("Help", "Get help", Material.EGG),
    EXIT("Exit", "Exit the menu", Material.BARRIER),
    RESET_SCALE("Reset Scale", "Set the scale to default", Material.PURPUR_PILLAR),
    FACE_NORTH("Change Side Facing North", "Change the side facing you of a blu3print to North", Material.COMPASS),
    FACE_SOUTH("Change Side Facing South", "Change the side facing you of a blu3print to South", Material.COMPASS),
    FACE_EAST("Change Side Facing East", "Change the side facing you of a blu3print to East", Material.COMPASS),
    FACE_WEST("Change Side Facing West", "Change the side facing you of a blu3print to West", Material.COMPASS),
    ;

    private String name;
    private String description;
    private Material material;
    private int amount;

    private MenuItems(String name, String description, Material material, int amount) {
        this.name = name;
        this.description = description;
        this.material = material;
        this.amount = amount;
    }

    private MenuItems(String name, String description, Material material) {
        this(name, description, material, 1);
    }

    public String getName() {
        return name;
    }

    public String getFormattedName() {
        return ChatColor.BLUE + name;
    }

    public String getDescription() {
        return description;
    }

    public String getFormattedDescription() {
        return ChatColor.DARK_BLUE + description;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MenuItems getMenuItem(String name) {
        if (name == null) {
            return null;
        }
        name = ChatColor.stripColor(name);
        for (MenuItems item : MenuItems.values()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

}
