package io.github.bl3rune.blueprints.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.config.PlayerBlu3printConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;
import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.data.ManipulatablePosition;
import io.github.bl3rune.blueprints.data.MaterialData;

public class Hologram {
    
    private static List<String> globalMaterialIgnoreList = new ArrayList<>();

    private Location location;
    private MaterialData [][][] selectionGrid;
    private ManipulatablePosition position;
    private List<ArmorStand> holograms; // List to hold the holograms
    private Function<Location,Location> calculateFinalLocationFunction;
    private PlayerBlu3printConfig config;
    private List<String> materialIgnoreList;

    public Hologram(Player player, Location startLocation, Blu3printData data, String blu3printUuid) {
        if  (globalMaterialIgnoreList.isEmpty()) {
            globalMaterialIgnoreList = GlobalConfig.getIgnoredMaterials();
        }
        String playerUUID = player.getUniqueId().toString();
        this.location = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
        this.selectionGrid = data.getSelectionGrid().clone();
        this.position = new ManipulatablePosition(data.getPosition(), data.getPosition().getScale());
        this.holograms = new ArrayList<>(); // Initialize the list of holograms
        calculateFinalLocationFunction = data.buildCalculateFinalLocationFunction(player, startLocation, true);
        config = Blueprints.getPlayerBlu3printConfig(playerUUID);
        this.materialIgnoreList = new ArrayList<>(); // Reset material ignore list
        if (config != null) {
            if (!config.uuidMatches(blu3printUuid)) {
                // Clear config
                config = null;
                Blueprints.setPlayerBlu3printConfig(playerUUID, null);
                player.sendMessage(ChatColor.RED + "Cleared blu3print config as using different blu3print!");
            } else {
                materialIgnoreList.addAll(config.getIgnoredMaterials());
            }
        }
        PlayerConfig playerConfig = Blueprints.getPlayerConfig(playerUUID);
        if (playerConfig != null) {   
            materialIgnoreList.addAll(playerConfig.getIgnoredMaterials());
        }
    }

    public void placeHologram() {
        // Implementation of placing a hologram
        
        int[] coords = position.next(true);
        int [] [] layers = null;
        if (config != null) {
            layers = config.getHologramViewLayers();
        }
        int scale = position.getScale();
        while (coords != null) {
            if (layers != null) {
                if (!withinLayers(layers, coords)) {
                    coords = position.next(true);
                    continue;
                }
            }
            MaterialData data = selectionGrid[coords[0] / scale][coords[1] / scale][coords[2] / scale];
            if (data == null || data.getMaterial() == null || isIgnorable(data.getMaterial())) {
                coords = position.next(true);
                continue;
            }
            Location loc = calculateFinalLocationFunction.apply(new Location(location.getWorld(), coords[2], coords[1], coords[0]));
            Location placeLocation = new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 0.1, loc.getZ() + 0.5);
            Block block = placeLocation.getBlock();
            if (block != null && !isIgnorable(block.getType())) {
                coords = position.next(true);
                continue;
            }

            buildArmourStand(placeLocation, data);
            coords = position.next(true);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                removeHologram();
            }
        }.runTaskLater(Blueprints.getInstance(), 20 * GlobalConfig.getHologramTtl());
    }

    private void buildArmourStand(Location l, MaterialData data) {
        // Implementation of building an armour stand
        ArmorStand armorStand = (ArmorStand) l.getWorld().spawn(l, ArmorStand.class, (ArmorStand a) -> {
            a.setVisible(false);
            a.setGravity(false);
            a.setCollidable(false);
            a.setArms(false);
            a.setBasePlate(false);
            a.setCanPickupItems(false);
            a.setMarker(true);
            a.setPersistent(false);
            a.setSmall(true);
            a.getEquipment().setHelmet(new ItemStack(data.getMaterial()));
            a.teleport(new Location(l.getWorld(), l.getX(), l.getY() - 0.5, l.getZ()));
        });
        this.holograms.add(armorStand);
    }

    private boolean withinLayers(int [][] layers, int[] coords) {
        int x = coords[2];
        int y = coords[1];
        int z = coords[0];
        boolean contains = false;

        if (layers[0] != null && layers[0].length > 0) {
            contains = false;
            for (int i : layers[0]) {
                if (i == x) contains = true;
            }
            if (contains == false) {
                return false;
            }
        }

        if (layers[1] != null && layers[1].length > 0) {
            contains = false;
            for (int i : layers[1]) {
                if (i == y) contains = true;
            }
            if (contains == false) {
                return false;
            }
        }

        if (layers[2] != null && layers[2].length > 0) {
            contains = false;
            for (int i : layers[2]) {
                if (i == z) contains = true;
            }
            if (contains == false) {
                return false;
            }
        }

        return true;
    }

    public void removeHologram() {
        this.holograms.forEach(a -> a.remove());
    }

    private boolean isIgnorable(Material material) {
        return material == null || material.isAir()
            || materialIgnoreList.contains(material.name().toUpperCase())
            || globalMaterialIgnoreList.contains(material.name().toUpperCase());
    }
    
}
