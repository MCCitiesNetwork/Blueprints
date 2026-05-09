package io.github.bl3rune.blueprints.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.core.ServiceRegistry;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.Rotation;
import io.github.bl3rune.blueprints.enums.Turn;
import io.github.bl3rune.blueprints.services.domain.BlockApplicationStrategy;
import io.github.bl3rune.blueprints.services.domain.InventoryCostCalculator;
import io.github.bl3rune.blueprints.services.domain.LimitValidator;
import io.github.bl3rune.blueprints.services.domain.MaterialIgnoreResolver;
import io.github.bl3rune.blueprints.services.domain.PlacementPlanner;
import io.github.bl3rune.blueprints.utils.EncodingUtils;
import io.github.bl3rune.blueprints.utils.Pair;

/**
 * Domain entity for a captured/imported blueprint. After Phase 4 of the
 * architecture overhaul this class holds state and delegates orchestration
 * (placement, ignore-material resolution, inventory accounting, limits, and
 * block application) to dedicated services in
 * {@code io.github.bl3rune.blueprints.services.domain}.
 */
public abstract class Blu3printData {

    protected MaterialData[][][] selectionGrid; // [z] [y] [x]
    protected Map<String, Integer> ingredientsCount; // key: material, value: count
    protected Map<String, String> ingredientsMap; // key: material, value: encoded
    protected Map<String, String> complexDataMap; // key: complex-mapping, value: complex-encoding
    protected List<String> materialIgnoreList; // player + per-blueprint ignored materials
    protected ManipulatablePosition position;
    protected String encoded;

    public MaterialData[][][] getSelectionGrid() {
        return selectionGrid;
    }

    public Map<String, Integer> getIngredientsCount() {
        return ingredientsCount;
    }

    public Map<String, String> getIngredientsMap() {
        return ingredientsMap;
    }

    public Map<String, String> getComplexDataMap() {
        return complexDataMap;
    }

    public ManipulatablePosition getPosition() {
        return position;
    }

    public String getEncodedString() {
        return encoded;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(ChatColor.WHITE).append("Ingredients:").append("\n").append(ChatColor.GRAY);
        ingredientsCount.forEach((k, v) -> sb.append(" - ")
                .append(k.replace("_", " "))
                .append(": ")
                .append(v * position.getScalingIngredientsMultiplier()).append("\n"));

        sb.append(ChatColor.WHITE).append("Position:").append("\n").append(ChatColor.GRAY);
        sb.append(" - X:Y:Z Sizes: ").append(position.getXSize() * position.getScale()).append(" : ");
        sb.append(position.getYSize() * position.getScale()).append(" : ");
        sb.append(position.getZSize() * position.getScale()).append("\n");
        sb.append(" - Orientation: ").append(position.getOrientation().name()).append("\n");
        sb.append(" - Rotation: ").append(position.getRotation().name()).append("\n");
        sb.append(" - Scale: ").append(position.getScale()).append("\n");
        return sb.toString();
    }

    // PLACING BLU3PRINT SECTION

    public void placeBlocks(Player player, Location location, boolean forced, boolean onTop, String blu3printUUID) {
        ServiceRegistry registry = registry();
        LimitValidator limits = registry.get(LimitValidator.class);
        if (!limits.playerAllowedToUse(player, position)) {
            return;
        }
        MaterialIgnoreResolver ignoreResolver = registry.get(MaterialIgnoreResolver.class);
        materialIgnoreList = ignoreResolver.resolvePlayerIgnoreList(player, blu3printUUID);

        Function<Location, Location> calculateFinalLocation = buildCalculateFinalLocationFunction(player, location,
                onTop);
        Map<String, Integer> blocksUnableToPlace = checkSpaceIsClear(calculateFinalLocation, ignoreResolver);

        InventoryCostCalculator costs = registry.get(InventoryCostCalculator.class);
        Map<String, Integer> missingBlocks = costs.checkPlayerHasBlocks(player, false, ingredientsCount,
                blocksUnableToPlace);
        if (!missingBlocks.isEmpty()) {
            sendMessage(player, ChatColor.RED + "Missing these blocks to place the blu3print:");
            missingBlocks
                    .forEach((k, v) -> sendMessage(player, ChatColor.RED + " - " + k.replace("_", " ") + " : " + v));
            return;
        }

        if (!blocksUnableToPlace.isEmpty()) {
            if (forced && GlobalConfig.isForcePlacementMessageEnabled()) {
                sendMessage(player, ChatColor.AQUA + "Forcing placing blu3print despite blocks in the way.");
            } else if (!forced) {
                sendMessage(player, ChatColor.RED + "You can't place the blu3print here. There are blocks in the way.");
                sendMessage(player,
                        ChatColor.RED + "To force placement of the blu3print, sneak while using the blu3print.");
                return;
            }
        }

        costs.checkPlayerHasBlocks(player, true, ingredientsCount, blocksUnableToPlace);

        BlockApplicationStrategy applier = registry.get(BlockApplicationStrategy.class);
        int[] coords = position.next(true);
        while (coords != null) {

            int scale = position.getScale();
            MaterialData data = selectionGrid[coords[0] / scale][coords[1] / scale][coords[2] / scale];
            if (data == null || data.getMaterial() == null
                    || ignoreResolver.isIgnorable(data.getMaterial(), materialIgnoreList)) {
                coords = position.next(true);
                continue;
            }

            Location placeLocation = calculateFinalLocation
                    .apply(new Location(location.getWorld(), coords[2], coords[1], coords[0]));
            Block block = placeLocation.getBlock();
            if (block != null && !ignoreResolver.isIgnorable(block.getType(), materialIgnoreList)) {
                coords = position.next(true);
                continue;
            }

            applier.place(player, placeLocation, data);
            coords = position.next(true);
        }
    }

    public Function<Location, Location> buildCalculateFinalLocationFunction(Player player, Location location,
            boolean onTop) {
        return registry().get(PlacementPlanner.class)
                .buildCalculateFinalLocationFunction(player, location, onTop, position);
    }

    private Map<String, Integer> checkSpaceIsClear(Function<Location, Location> calculateFinalLocation,
            MaterialIgnoreResolver ignoreResolver) {
        int scale = position.getScale();
        int[] coords = position.next(true);
        Map<String, Integer> blocksUnableToPlace = new HashMap<>();
        while (coords != null) {
            MaterialData materialData = this.selectionGrid[coords[0] / scale][coords[1] / scale][coords[2] / scale];
            if (materialData == null || materialData.getName() == null
                    || ignoreResolver.isIgnorable(materialData.getMaterial(), materialIgnoreList)) {
                coords = position.next(true);
                continue;
            }
            Location loc = calculateFinalLocation.apply(new Location(null, coords[2], coords[1], coords[0]));
            Block block = loc.getBlock();
            if (block != null && !ignoreResolver.isIgnorable(block.getType(), materialIgnoreList)) {
                Integer count = blocksUnableToPlace.getOrDefault(materialData.getMaterial().name(), 0);
                blocksUnableToPlace.put(materialData.getMaterial().name(), count + 1);
            }
            coords = position.next(true);
        }
        return blocksUnableToPlace;
    }

    // EDITING BLU3PRINT SECTION

    public String updateEncodingWithTurn(Turn turn) {
        int s = position.getScale();
        Pair<Orientation, Rotation> turned = position.calculateTurn(turn);
        int[] newSizes = position.getNewSizes(turned.getA());
        newSizes = position.getNewSizes(turned.getB(), newSizes);
        return updateManipulatablePosition(
                new ManipulatablePosition(newSizes[0], newSizes[1], newSizes[2], turned.getA(), turned.getB(), s));
    }

    public String updateEncodingWithOrientation(Orientation newOrientation) {
        Rotation r = position.getRotation();
        int s = position.getScale();
        int[] newSizes = position.getNewSizes(newOrientation);
        return updateManipulatablePosition(
                new ManipulatablePosition(newSizes[0], newSizes[1], newSizes[2], newOrientation, r, s));
    }

    public String updateEncodingWithRotation(Rotation newRotation) {
        Orientation o = position.getOrientation();
        int s = position.getScale();
        int[] newSizes = position.getNewSizes(newRotation);
        return updateManipulatablePosition(
                new ManipulatablePosition(newSizes[0], newSizes[1], newSizes[2], o, newRotation, s));
    }

    public String updateEncodingWithScale(Player player, int newScale) {
        if (!registry().get(LimitValidator.class).newScaleAllowed(player, position, newScale)) {
            return null;
        }

        int scale = newScale / position.getScale();
        if (scale != 1) {
            Map<String, Integer> newCount = new HashMap<>();
            this.ingredientsCount.forEach((k, v) -> newCount.put(k, v * scale));
            this.ingredientsCount = newCount;
        }
        return updateManipulatablePosition(new ManipulatablePosition(position, newScale));
    }

    private String updateManipulatablePosition(ManipulatablePosition newPosition) {
        String bodyString = EncodingUtils.getBodyFromEncoding(encoded);
        String newHeader = EncodingUtils
                .buildHeaderWithPerspective(EncodingUtils.ingredientsMapToString(ingredientsMap), newPosition);
        String newEncoding = EncodingUtils.buildEncodedString(newHeader, bodyString);
        sendMessage(null, "Updated blu3print: " + newEncoding);
        return newEncoding;
    }

    // UTILITY METHODS

    protected void sendMessage(Player player, String message) {
        if (player == null) {
            Blueprints.logger().info(message);
        } else {
            player.sendMessage(message);
        }
    }

    protected List<String> buildMaterialIgnoreList(Player player, String blu3printUUID) {
        return registry().get(MaterialIgnoreResolver.class).resolvePlayerIgnoreList(player, blu3printUUID);
    }

    protected boolean isIgnorable(Material material) {
        return registry().get(MaterialIgnoreResolver.class).isIgnorable(material, materialIgnoreList);
    }

    protected boolean playerAllowedToUse(Player player) {
        return registry().get(LimitValidator.class).playerAllowedToUse(player, position);
    }

    protected boolean sizesExceedLimit(int[] sizes, int scale, int max) {
        return registry().get(LimitValidator.class).sizesExceedLimit(sizes, scale, max);
    }

    private static ServiceRegistry registry() {
        return Blueprints.getInstance().getServiceRegistry();
    }
}
