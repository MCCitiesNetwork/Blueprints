package io.github.bl3rune.blueprints.data;

import org.bukkit.Material;
import io.github.bl3rune.blueprints.enums.AttachedFace;
import io.github.bl3rune.blueprints.enums.Axis;
import io.github.bl3rune.blueprints.enums.Half;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.RailShape;
import io.github.bl3rune.blueprints.enums.SlabType;

import java.util.Set;

public abstract class MaterialData {

    protected String namespace = null;
    protected String name; // (optional) encodedComplexData : (optional) nsKey : materialName
    protected Material material;
    protected String complexData; // [ key=value, key=value, ... ]
    protected String encodedComplexData; // k=v,k=v,...
    protected Orientation face = null;
    protected Orientation rotation = null;
    protected Axis axis = null;
    protected Set<Orientation> multiFaces = null;
    protected AttachedFace attachedFace = null;
    protected Half half = null;
    protected SlabType slabType = null;
    protected RailShape railShape = null;
    protected int count;

    protected String removeDefaultValue(String raw, String key) {
        if (raw.contains("," + key)) {
            return raw.replace("," + key, "");
        }
        if (raw.contains(key + ",")) {
            return raw.replace(key + ",", "");
        }
        return raw.replace(key, "");
    }

    protected String addKey(String raw, String key) {
        if (raw.isEmpty()) {
            return key;
        }
        return raw + "," + key;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }

    public Orientation getFace()  {
        return face;
    }

    public int getCount()  {
        return count;
    }

    public void setCount(int count)  {
        this.count = count;
    }

    public String getComplexData() {
        return complexData;
    }

    public String getEncodedComplexData() {
        return encodedComplexData;
    }

    public Orientation getRotation() {
        return rotation;
    }

    public Axis getAxis() {
        return axis;
    }

    public Set<Orientation> getMultiFaces() {
        return multiFaces;
    }

    public AttachedFace getAttachedFace() {
        return attachedFace;
    }

    public Half getHalf() {
        return half;
    }

    public SlabType getSlabType() {
        return slabType;
    }

    public RailShape getRailShape() {
        return railShape;
    }

}
