package io.github.bl3rune.blueprints.data;

import static io.github.bl3rune.blueprints.utils.EncodingUtils.MODIFIER;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Slab;

import io.github.bl3rune.blueprints.enums.AttachedFace;
import io.github.bl3rune.blueprints.enums.Axis;
import io.github.bl3rune.blueprints.enums.Half;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.RailShape;
import io.github.bl3rune.blueprints.enums.SlabType;

public class CapturedMaterialData extends MaterialData {

    private static final Set<String> defaultValuesToRemove = Set.of(
        "shape=straight",
        "waterlogged=false",
        "distance=0",
        "bottom=false",
        "has_record=false",
        "delay=1",
        "locked=false",
        "powered=false",
        "occupied=false",
        "lit=false",
        "power=0",
        "east=none",
        "north=none",
        "south=none",
        "west=none",
        "shape=north_south",
        "type=bottom",
        "stage=1",
        "honey_level=0",
        "in_wall=false",
        "open=false",
        "up=true",
        "snowy=false",
        "hinge=left"
    );

    public CapturedMaterialData(Block block) {
        super.material = block.getType();
        super.count = 1;
        super.name = material.name();
        super.namespace = null;
        if (!material.getKey().getNamespace().equals("minecraft")) {
            super.namespace = material.getKey().getNamespace();
            super.name = super.namespace + MODIFIER + super.name;
        }
        buildComplexFields(block.getBlockData());
    }

    private void buildComplexFields(BlockData blockData) {
        String rawBlockData = blockData.getAsString();
        if (!rawBlockData.contains("[")) {
            return;
        }
        complexData = rawBlockData + "";
        rawBlockData = rawBlockData.substring(rawBlockData.indexOf("[")+1, rawBlockData.indexOf("]"));
        rawBlockData = removeDirectionalBlockData(rawBlockData);
        rawBlockData = removeRotationBlockData(rawBlockData);
        rawBlockData = removeOrientableData(rawBlockData);
        rawBlockData = removeMultiFacingBlockData(rawBlockData);
        rawBlockData = removeFaceAttachableBlockData(rawBlockData);
        rawBlockData = removeBisectedBlockData(rawBlockData);
        rawBlockData = removeSlabBlockData(rawBlockData);
        rawBlockData = removeRailBlockData(rawBlockData);

        for (String defaultValue : defaultValuesToRemove) {
            rawBlockData = removeDefaultValue(rawBlockData, defaultValue);
        }
        
        if (blockData instanceof Directional ) {
            face = Orientation.getOrientation(((Directional) blockData).getFacing());
            rawBlockData = addKey(rawBlockData, face.getDirectionalShort());
        }
        if (blockData instanceof Rotatable) {
            this.rotation = Orientation.getOrientation(((Rotatable) blockData).getRotation());
            rawBlockData = addKey(rawBlockData, rotation.getRotatableShort());
        }
        if (blockData instanceof Orientable) {
            this.axis = Axis.fromBukkit(((Orientable) blockData).getAxis());
            rawBlockData = addKey(rawBlockData, this.axis.getShortName());
        }
        if (blockData instanceof MultipleFacing) {
            multiFaces = ((MultipleFacing) blockData).getFaces().stream().map(f -> Orientation.getOrientation(f)).collect(Collectors.toSet());
        }
        if (blockData instanceof FaceAttachable) {
            attachedFace = AttachedFace.fromBukkit(((FaceAttachable) blockData).getAttachedFace());
            rawBlockData = addKey(rawBlockData, attachedFace.getShortName());
        }
        if (blockData instanceof Bisected) {
            half = Half.fromBukkit(blockData);
            rawBlockData = addKey(rawBlockData, half.getShortName());
        }
        if (blockData instanceof Slab) {
            slabType = SlabType.fromType(((Slab) blockData).getType());
            rawBlockData = addKey(rawBlockData, slabType.getShortName());
        }
        if (blockData instanceof Rail) {
            railShape = RailShape.fromBukkit(((Rail)blockData).getShape());
            rawBlockData = addKey(rawBlockData, railShape.getShortName());
        }
        
        this.name = rawBlockData.equals("") ? name : rawBlockData + MODIFIER + name;
        this.encodedComplexData = rawBlockData.equals("") ? null : rawBlockData;
    }


    private String removeDirectionalBlockData(String rawBlockData) {
        for (BlockFace bf : BlockFace.values()) {
            rawBlockData = removeDefaultValue(rawBlockData, "facing=" + bf.name().toLowerCase());
        }
        return rawBlockData;
    }

    private String removeRotationBlockData(String rawBlockData) {
        for (int i = 0; i < 20; i++) {
            rawBlockData = removeDefaultValue(rawBlockData, "rotation=" + i);
        }
        return rawBlockData;
    }

    private String removeOrientableData(String rawBlockData) {
        for (Axis a : Axis.values()) {
            rawBlockData = removeDefaultValue(rawBlockData, a.getFullName());
        }
        return rawBlockData;
    }

    private String removeMultiFacingBlockData(String rawBlockData) {
        for (Orientation o : Orientation.values()) {
            rawBlockData = removeDefaultValue(rawBlockData, o.name().toLowerCase() + "=false");
            // LEAVE IN TRUE FOR NOW - NO WAY TO COMPRESS
        }
        return rawBlockData;
    }

    private String removeFaceAttachableBlockData(String rawBlockData) {
        for (AttachedFace af : AttachedFace.values()) {
            rawBlockData = removeDefaultValue(rawBlockData, af.getFullName());
        }
        return rawBlockData;
    }

    private String removeBisectedBlockData(String rawBlockData) {
        for (Half b : Half.values()) {
            rawBlockData = removeDefaultValue(rawBlockData, b.getFullName());
        }
        return rawBlockData;
    }

    private String removeSlabBlockData(String rawBlockData) {
        for (SlabType st : SlabType.values()) {
            rawBlockData = removeDefaultValue(rawBlockData, st.getFullName());
        }
        return rawBlockData;
    }

    private String removeRailBlockData(String rawBlockData) {
        for (RailShape rs : RailShape.values()) {
            rawBlockData  = removeDefaultValue(rawBlockData, rs.getFullName());
        }
        return rawBlockData;
    }
}
