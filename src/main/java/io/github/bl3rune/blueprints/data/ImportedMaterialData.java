package io.github.bl3rune.blueprints.data;

import static io.github.bl3rune.blueprints.utils.EncodingUtils.MODIFIER;

import java.util.HashSet;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

import io.github.bl3rune.blueprints.enums.AttachedFace;
import io.github.bl3rune.blueprints.enums.Axis;
import io.github.bl3rune.blueprints.enums.Half;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.RailShape;
import io.github.bl3rune.blueprints.enums.SlabType;

public class ImportedMaterialData extends MaterialData {

    public ImportedMaterialData(String name, Material material, String encodedComplexData, int count) {
        this.name = name;
        this.material = material;
        this.count = count;
        if (encodedComplexData != null && !encodedComplexData.isEmpty()) {
            parseComplexData(encodedComplexData);
        }
    }

    protected void parseComplexData(String encodedComplexData) {
        this.encodedComplexData = encodedComplexData;
        String[] data = encodedComplexData.split(Pattern.quote(","));
        for (String s : data) {
            face = face != null ? face : Orientation.findOrientationInComplexDataString(s);
            rotation = rotation != null ? rotation : Orientation.findRotatableInComplexDataString(s);
            axis = axis != null ? axis : Axis.findInComplexDataString(s);
            Orientation mf = Orientation.findMultiFacingInComplexDataString(s);
            if (mf != null) {
                if (this.multiFaces == null) {
                    this.multiFaces = new HashSet<>();
                }
                multiFaces.add(mf);
            }
            attachedFace = attachedFace != null ? attachedFace : AttachedFace.findInComplexDataString(s);
            half = half != null ? half : Half.findInComplexDataString(s);
            slabType = slabType != null ? slabType : SlabType.findInComplexDataString(s);
            railShape = railShape != null ? railShape : RailShape.findInComplexDataString(s);
        }
        String encodedCopy = encodedComplexData.toLowerCase();
        encodedCopy = face == null ? encodedCopy : encodedCopy.replace(face.getDirectionalShort(), face.getDirectional());
        encodedCopy = rotation == null ? encodedCopy : encodedCopy.replace(rotation.getRotatableShort(), rotation.getRotatable());
        encodedCopy = axis == null ? encodedCopy : encodedCopy.replace(axis.getShortName(), axis.getFullName());
        encodedCopy = attachedFace == null ? encodedCopy : encodedCopy.replace(attachedFace.getShortName(), attachedFace.getFullName());
        encodedCopy = half == null ? encodedCopy : encodedCopy.replace(half.getShortName(), half.getFullName());
        encodedCopy = slabType == null ? encodedCopy : encodedCopy.replace(slabType.getShortName(), slabType.getFullName());
        encodedCopy = railShape == null ? encodedCopy : encodedCopy.replace(railShape.getShortName(), railShape.getFullName());
        NamespacedKey ns = material.getKey();
        this.complexData = ns.getNamespace() + MODIFIER + ns.getKey() + "[" + encodedCopy + "]";
    }

}
