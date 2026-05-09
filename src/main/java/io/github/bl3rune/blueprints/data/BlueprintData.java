package io.github.bl3rune.blueprints.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Domain entity for a captured/imported blueprint. After Phase 4 of the
 * architecture overhaul this class holds state and delegates orchestration
 * (placement, ignore-material resolution, inventory accounting, limits, and
 * block application) to dedicated services in
 * {@code io.github.bl3rune.blueprints.services.domain}.
 */
public abstract class BlueprintData {

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

    public Component describe() {
        StringBuilder ingredients = new StringBuilder();
        ingredientsCount.forEach((k, v) -> ingredients.append(" - ")
                .append(k.replace("_", " "))
                .append(": ")
                .append(v * position.getScalingIngredientsMultiplier()).append("\n"));

        StringBuilder positionDetails = new StringBuilder();
        positionDetails.append(" - X:Y:Z Sizes: ").append(position.getXSize() * position.getScale()).append(" : ");
        positionDetails.append(position.getYSize() * position.getScale()).append(" : ");
        positionDetails.append(position.getZSize() * position.getScale()).append("\n");
        positionDetails.append(" - Orientation: ").append(position.getOrientation().name()).append("\n");
        positionDetails.append(" - Rotation: ").append(position.getRotation().name()).append("\n");
        positionDetails.append(" - Scale: ").append(position.getScale()).append("\n");

        return Component.text()
                .append(Component.text("Ingredients:\n", NamedTextColor.WHITE))
                .append(Component.text(ingredients.toString(), NamedTextColor.GRAY))
                .append(Component.text("Position:\n", NamedTextColor.WHITE))
                .append(Component.text(positionDetails.toString(), NamedTextColor.GRAY))
                .build();
    }

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(describe());
    }

    // PLACING BLU3PRINT SECTION

    public void placeBlocks(Player player, Location location, boolean forced, boolean onTop, String blueprintUUID) {
        ServiceRegistry registry = registry();
        LimitValidator limits = registry.get(LimitValidator.class);
        if (!limits.playerAllowedToUse(player, position)) {
            return;
        }
        MaterialIgnoreResolver ignoreResolver = registry.get(MaterialIgnoreResolver.class);
        materialIgnoreList = ignoreResolver.resolvePlayerIgnoreList(player, blueprintUUID);

        Function<Location, Location> calculateFinalLocation = buildCalculateFinalLocationFunction(player, location,
                onTop);
        Map<String, Integer> blocksUnableToPlace = checkSpaceIsClear(calculateFinalLocation, ignoreResolver);

        InventoryCostCalculator costs = registry.get(InventoryCostCalculator.class);
        Map<String, Integer> missingBlocks = costs.checkPlayerHasBlocks(player, false, ingredientsCount,
                blocksUnableToPlace);
        if (!missingBlocks.isEmpty()) {
            sendMessage(player, Component.text("Missing these blocks to place the blueprint:", NamedTextColor.RED));
            missingBlocks
                    .forEach((k, v) -> sendMessage(player,
                            Component.text(" - " + k.replace("_", " ") + " : " + v, NamedTextColor.RED)));
            return;
        }

        if (!blocksUnableToPlace.isEmpty()) {
            if (forced && GlobalConfig.isForcePlacementMessageEnabled()) {
                sendMessage(player, Component.text("Forcing placing blueprint despite blocks in the way.",
                        NamedTextColor.AQUA));
            } else if (!forced) {
                sendMessage(player, Component.text(
                        "You can't place the blueprint here. There are blocks in the way.", NamedTextColor.RED));
                sendMessage(player, Component.text(
                        "To force placement of the blueprint, sneak while using the blueprint.", NamedTextColor.RED));
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
        sendMessage(null, Component.text("Updated blueprint: " + newEncoding));
        return newEncoding;
    }

    // UTILITY METHODS

    protected void sendMessage(Player player, Component message) {
        if (player == null) {
            Blueprints.logger().info(PlainTextComponentSerializer.plainText().serialize(message));
        } else {
            player.sendMessage(message);
        }
    }

    protected List<String> buildMaterialIgnoreList(Player player, String blueprintUUID) {
        return registry().get(MaterialIgnoreResolver.class).resolvePlayerIgnoreList(player, blueprintUUID);
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
