package io.github.bl3rune.blueprints.data;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.utils.EdgeCaseBlockUtils;
import io.github.bl3rune.blueprints.utils.EncodingUtils;

import static io.github.bl3rune.blueprints.utils.EncodingUtils.ROW_END;
import static io.github.bl3rune.blueprints.utils.EncodingUtils.COLUMN_END;
import static io.github.bl3rune.blueprints.utils.EncodingUtils.DOUBLE_CHARACTER;
import static io.github.bl3rune.blueprints.utils.EncodingUtils.MODIFIER;

public class ImportedBlueprintData extends BlueprintData {

    public ImportedBlueprintData(Player player, String encodedString, String blu3printUUID) {

        materialIgnoreList = buildMaterialIgnoreList(player, blu3printUUID);

        this.encoded = encodedString;
        String header = EncodingUtils.getHeaderFromEncoding(encodedString);
        this.ingredientsMap = EncodingUtils.getIngredientsMapFromHeader(header);
        if (this.ingredientsMap.isEmpty()) {
            sendMessage(player,"Invalid Blu3print data provided! @ingredients");
            return;
        }
        
        int [] sizes = EncodingUtils.getSizesFromHeader(header);
        if (sizes.length < 3) {
            sendMessage(player,"Invalid Blu3print data provided! @sizes");
            return;
        }
        this.ingredientsCount = new HashMap<>();

        ManipulatablePosition dData = EncodingUtils.getDirectionalDataFromHeader(header);

        this.position = new ManipulatablePosition(sizes[2], sizes[1], sizes[0], dData.getOrientation(), dData.getRotation(), dData.getScale());

        if (!playerAllowedToUse(player)) {
            return;
        }

        buildSelectionGrid(EncodingUtils.getBodyFromEncoding(encodedString));
        if (dData.getScale() > 1) {
            final int scaleFinal = dData.getScale();
            Map<String, Integer> newCount = new HashMap<>();
            this.ingredientsCount.forEach((k,v) -> newCount.put(k, v  * scaleFinal));
            this.ingredientsCount = newCount;
        }
    }

    private void buildSelectionGrid(String body) {
        this.selectionGrid = new MaterialData[position.getZSize()][position.getYSize()][position.getXSize()];
        String prev = null;
        for (char c : body.toCharArray()) {
            String s = String.valueOf(c);
            if (s.equals(ROW_END) || s.equals(COLUMN_END)) {
                fillRemainingRow(position, prev);
                prev = null;
            } else if (prev == null) {
                prev = s;
            } else if (prev.startsWith(DOUBLE_CHARACTER) && prev.length() < 3) { 
                prev = prev + s;
            } else if (EncodingUtils.isEncoded(s) || s.equals(DOUBLE_CHARACTER)) {
                addToSelection(position, prev);
                prev = s;
            } else { // A NUMBER
                prev = prev + s;
            }
        }
    }

    private void addToSelection(ManipulatablePosition position, String encoded) {
        MaterialData materialData = decodeBlock(encoded);
        for (int i = 0; i < materialData.getCount(); i++)  {
            int[] coords =  position.next(false);
            this.selectionGrid[coords[0]][coords[1]][coords[2]] = materialData;
        }
        if (materialData.getName() != null) {
            int existing = this.ingredientsCount.getOrDefault(materialData.getMaterial().name(), 0);
            int toAdd = EdgeCaseBlockUtils.getEdgeCaseIngredientCountToAdd(materialData);
            this.ingredientsCount.put(materialData.getMaterial().name(), existing + toAdd);
        }
    }

    private void fillRemainingRow(ManipulatablePosition position, String encoded) {
        MaterialData materialData = decodeBlock(encoded);
        int added = 0;
        while(added == 0 || !position.endOfInnerLoop()) {
            int[] coords =  position.next(false);
            this.selectionGrid[coords[0]][coords[1]][coords[2]] = materialData;
            added++;
        }
        if (materialData.getName() != null) {
            int existing = this.ingredientsCount.getOrDefault(materialData.getMaterial().name(), 0);
            materialData.setCount(added);
            int toAdd = EdgeCaseBlockUtils.getEdgeCaseIngredientCountToAdd(materialData);
            this.ingredientsCount.put(materialData.getMaterial().name(), existing + toAdd);
        }
    }

    private MaterialData decodeBlock(String encoded)  {
        String code;
        int count;

        if (encoded == null) {
            return new ImportedMaterialData(null, Material.AIR, null, 1);
        }

        if (encoded.startsWith(DOUBLE_CHARACTER)) {
            code = encoded.substring(1, 3);
            count = encoded.length() > 3 ? Integer.parseInt(encoded.substring(3)) : 1;
        } else {
            code = encoded.substring(0,1);
            count = encoded.length() > 1 ? Integer.parseInt(encoded.substring(1)) : 1;
        }

        String decoded = ingredientsMap.entrySet().stream()
            .filter(e -> e.getValue().equals(code))
                .map(e -> e.getKey()).findFirst()
                    .orElse(null);

        if (decoded == null) {
            return new ImportedMaterialData(null, Material.AIR, null, count);
        }

        String encodedComplexData = null;
        Material material = null;

        String[] split = EncodingUtils.modifierSplit(decoded);
        if (split.length > 1) {
            if (!split[0].contains("=")) {
                material = Material.matchMaterial(decoded.toLowerCase());
            } else {
                material = Material.matchMaterial(decoded.substring(decoded.indexOf(MODIFIER) + 1));
                encodedComplexData = split[0];
            }
        } else {
            material = Material.matchMaterial(decoded);
        }
        return new ImportedMaterialData(decoded, material, encodedComplexData, count);
    }

}
