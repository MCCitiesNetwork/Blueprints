package io.github.bl3rune.blueprints.items;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.config.PlayerBlueprintConfig;
import io.github.bl3rune.blueprints.config.PlayerConfig;
import io.github.bl3rune.blueprints.data.BlueprintData;
import io.github.bl3rune.blueprints.data.ManipulatablePosition;
import io.github.bl3rune.blueprints.data.MaterialData;
import io.github.bl3rune.blueprints.services.domain.MaterialIgnoreResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Hologram {

    private Location location;
    private MaterialData [][][] selectionGrid;
    private ManipulatablePosition position;
    private List<ItemDisplay> holograms;
    private Function<Location,Location> calculateFinalLocationFunction;
    private PlayerBlueprintConfig config;
    private List<String> materialIgnoreList;
    private final MaterialIgnoreResolver ignoreResolver;

    public Hologram(Player player, Location startLocation, BlueprintData data, String blu3printUuid) {
        this.ignoreResolver = Blueprints.getInstance().getServiceRegistry().get(MaterialIgnoreResolver.class);
        String playerUUID = player.getUniqueId().toString();
        this.location = new Location(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ());
        this.selectionGrid = data.getSelectionGrid().clone();
        this.position = new ManipulatablePosition(data.getPosition(), data.getPosition().getScale());
        this.holograms = new ArrayList<>();
        calculateFinalLocationFunction = data.buildCalculateFinalLocationFunction(player, startLocation, true);
        config = Blueprints.getPlayerBlueprintConfig(playerUUID);
        this.materialIgnoreList = new ArrayList<>();
        if (config != null) {
            if (!config.uuidMatches(blu3printUuid)) {
                config = null;
                Blueprints.setPlayerBlueprintConfig(playerUUID, null);
                player.sendMessage(Component.text("Cleared blueprint config as using different blueprint!", NamedTextColor.RED));
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
            if (data == null || data.getMaterial() == null
                    || ignoreResolver.isIgnorable(data.getMaterial(), materialIgnoreList)) {
                coords = position.next(true);
                continue;
            }
            Location loc = calculateFinalLocationFunction.apply(new Location(location.getWorld(), coords[2], coords[1], coords[0]));
            Location placeLocation = new Location(loc.getWorld(), loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5);
            Block block = placeLocation.getBlock();
            if (block != null && !ignoreResolver.isIgnorable(block.getType(), materialIgnoreList)) {
                coords = position.next(true);
                continue;
            }

            buildDisplay(placeLocation, data);
            coords = position.next(true);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                removeHologram();
            }
        }.runTaskLater(Blueprints.getInstance(), 20 * GlobalConfig.getHologramTtl());
    }

    private void buildDisplay(Location l, MaterialData data) {
        ItemDisplay display = l.getWorld().spawn(l, ItemDisplay.class, d -> {
            d.setItemStack(new ItemStack(data.getMaterial()));
            d.setItemDisplayTransform(ItemDisplayTransform.NONE);
            d.setPersistent(false);
            d.setBrightness(new Brightness(15, 15));
            d.setTransformation(new Transformation(
                    new Vector3f(-0.5f, -0.5f, -0.5f),
                    new AxisAngle4f(0f, 0f, 0f, 1f),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f(0f, 0f, 0f, 1f)));
        });
        this.holograms.add(display);
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
}
