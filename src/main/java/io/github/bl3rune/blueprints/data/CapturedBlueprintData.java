package io.github.bl3rune.blueprints.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.Rotation;
import io.github.bl3rune.blueprints.utils.EdgeCaseBlockUtils;
import io.github.bl3rune.blueprints.utils.EncodingUtils;
import io.github.bl3rune.blueprints.utils.LocationUtils;
import io.github.bl3rune.blueprints.utils.Pair;

import static io.github.bl3rune.blueprints.utils.EncodingUtils.VOID;
import static io.github.bl3rune.blueprints.utils.EncodingUtils.ROW_END;
import static io.github.bl3rune.blueprints.utils.EncodingUtils.COLUMN_END;

public class CapturedBlueprintData extends BlueprintData {

    public CapturedBlueprintData(Player player, String pos1, String pos2, String blu3printUUID) {

        materialIgnoreList = buildMaterialIgnoreList(player, blu3printUUID);

        Location loc1 = LocationUtils.getCoordsFromPosString(pos1);
        Location loc2 = LocationUtils.getCoordsFromPosString(pos2);

        String world = loc1.getWorld().getName();
        if (!world.equals(loc2.getWorld().getName())) {
            sendMessage(player,ChatColor.RED + "I didn't think I would have to say this...");
            sendMessage(player,ChatColor.RED + "...");
            sendMessage(player,ChatColor.RED + "But the two locations have to be in the same world...");
            sendMessage(player,ChatColor.RED + "...");
            sendMessage(player,ChatColor.RED + "...");
            sendMessage(player,ChatColor.RED + "Take a break for a bit friend.");
            return;
        }

        int[] locX = LocationUtils.reorderNegativeCoords(loc1.getBlockX(), loc2.getBlockX());
        int[] locY = LocationUtils.reorderNegativeCoords(loc1.getBlockY(), loc2.getBlockY());
        int[] locZ = LocationUtils.reorderNegativeCoords(loc1.getBlockZ(), loc2.getBlockZ());

        int xSize = locX[1] - locX[0] + 1;
        int ySize = locY[1] - locY[0] + 1;
        int zSize = locZ[1] - locZ[0] + 1;

        Integer maxSize = GlobalConfig.getMaxSize();
         if (player != null && maxSize != null && sizesExceedLimit(new int[] {xSize,ySize,zSize}, 1, maxSize)) {
             if (!player.hasPermission("blu3print.no-size-limit")) {
                 sendMessage(player,ChatColor.RED + "You do not have permission to set size over the max size limit of " + maxSize + "!");
                 return;
             }
        }

        List<String> ignoreBlocks = io.github.bl3rune.blueprints.Blueprints.getInstance()
                .getServiceRegistry()
                .get(io.github.bl3rune.blueprints.services.InteractionCooldownService.class)
                .getIgnoreList(player.getUniqueId().toString());

        selectionGrid = new MaterialData[zSize][ySize][xSize];
        this.ingredientsCount = new HashMap<>();
        this.ingredientsMap = new HashMap<>();
        Map<String, Integer> ingredientsCountComplex = new HashMap<>();

        for (int z = 0; z < zSize; z++) {
            for (int y = 0; y < ySize; y++) {
                for (int x = 0; x < xSize; x++) {
                    Block block = Bukkit.getWorld(world).getBlockAt(locX[0] + x, locY[0] + y, locZ[0] + z);
                    if (block == null || isIgnorable(block.getType()) || ignoreBlocks.stream().anyMatch(
                                i -> LocationUtils.locationsMatch(LocationUtils.getCoordsFromPosString(i), block.getLocation())
                            )) {
                        selectionGrid[z][y][x] = new ImportedMaterialData(null, Material.AIR, null, 1);
                    } else {
                        String blockName = block.getType().name();
                        MaterialData materialData = new CapturedMaterialData(block);
                        int toAdd = EdgeCaseBlockUtils.getEdgeCaseIngredientCountToAdd(materialData);
                        ingredientsCount.put(blockName, ingredientsCount.getOrDefault(blockName, 0) + toAdd);
                        ingredientsCountComplex.put(materialData.getName(), ingredientsCountComplex.getOrDefault(materialData.getName(), 0) + 1);
                        selectionGrid[z][y][x] = materialData;
                    }
                }
            }
        }

        this.ingredientsMap = EncodingUtils.buildIngredientsMapFromIngredientsCount(ingredientsCountComplex);
        String header = EncodingUtils.ingredientsMapToString(ingredientsMap);

        List<ManipulatablePosition> positions = new ArrayList<>();
        for (Orientation o : Orientation.values()) {
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.TOP));
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.RIGHT));
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.BOTTOM));
            positions.add(new ManipulatablePosition(zSize, ySize, xSize, o, Rotation.LEFT));
        }
        Pair<String, ManipulatablePosition> bestPosition = positions.parallelStream().map(p -> new Pair<String,ManipulatablePosition>(do3dLoop(p), p)).sorted(
            (p1,p2) -> Integer.compare(p1.getA().length(), p2.getA().length())
        ).findFirst().get();

        this.position = bestPosition.getB();
        header = EncodingUtils.buildHeaderWithPerspective(header, position);
        
        this.encoded = EncodingUtils.buildEncodedString(header, bestPosition.getA());
    }

    private String do3dLoop(ManipulatablePosition position) {
        StringBuilder sb = new StringBuilder();
        String previous = null;
        int count = 0;
        boolean empty = true;
        int [] coords = position.next(false);
        while(coords !=  null) {
            MaterialData materialData = selectionGrid[coords[0]][coords[1]][coords[2]];
            if (materialData == null) {
                materialData = new ImportedMaterialData(null, Material.AIR, null, 1);
            }
            String name = materialData.getName();
            sb.append(determineEncoding(previous, name, count));
            if ((previous == null && name == null) || (name != null && name.equals(previous))) {
                count++;
             } else {
                empty = false;
                previous = name;
                count = 1;
             }

             if (position.endOfInnerLoop()) {
                // ON EMPTY ROW should look like : ROW_END + ROW_END ( or COLUMN_END )
                // OTHERWISE : ... + LAST_MAT (no number required) + ROW_END ( or COLUMN_END )
                sb.append(empty ? "" : determineEncoding(previous, "", 1));
                previous = null;
                count = 0;
                sb.append(position.endOfMiddleLoop() ? COLUMN_END : ROW_END);
                empty = true;
            }
            coords = position.next(false);
        }
        
        return sb.toString();
    }

    /**
     * Translates structures using the ingredientsMap into an encoded text string
     * 
     * on count zero : no change so return blank string
     * on prev null and current not null: record the null records as AIR (with a count if more than once)
     * on prev not null and current is different : record the previous material  (with a count if more than once)
     * otherwise : return blank string
     * 
     * @param prev    Previous material name
     * @param current Current material name
     * @param count   How many times the previous block type has appeared in a row
     * @return encodedString describing material change or blank string if no change
     */
    private String determineEncoding(String prev, String current, int count) {
        if (count == 0) return "";
        if (prev == null) {
            if (current != null) return VOID + (count < 2 ? "" : count);
        } else if (!prev.equals(current)) {
            return ingredientsMap.getOrDefault(prev, VOID) + (count < 2 ? "" : count);
        }
        return "";
    }

}
