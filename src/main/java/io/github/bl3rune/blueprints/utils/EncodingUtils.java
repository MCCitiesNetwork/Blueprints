package io.github.bl3rune.blueprints.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.github.bl3rune.blueprints.data.ManipulatablePosition;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.Rotation;

public class EncodingUtils {

    private static final String HEADER_END = "~";
    public static final String COLUMN_END = "|";
    public static final String ROW_END = "-";
    public static final String DOUBLE_CHARACTER = ".";
    public static final String MAPS_TO = "=";
    public static final String MODIFIER = ":";
    public static final String CONDENSED = "!";
    public static final String VOID = "A";

    public static final String [] BLU3_ENCODE = new String[] {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d",
            "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "w", "x",
            "y", "z"
    };

    // ENCODED STRING = HEADER + HEADER_END + BODY
    public static String getHeaderFromEncoding(String encoding) {
        String [] split = split(encoding, HEADER_END);
        return split[0];
    }

    public static String getBodyFromEncoding(String encoding) {
        String [] split = split(encoding, HEADER_END);
        if (split.length > 1) return split[1];
        return "";
    }

    public static String buildEncodedString(String header, String body) {
        return header + HEADER_END + body;
    }

    // HEADER = INGREDIENTS + SIZES + DIRECTIONAL DATA + CONDENSATION DATA
    public static String ingredientsMapToString(Map<String,String> map) { // key: material, value: encoded
        StringBuilder sb = new StringBuilder();
        map.forEach((k,v) -> {
            sb.append(v);
            sb.append(MAPS_TO);
            sb.append(k);
            sb.append(ROW_END);
        });
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static Map<String,String> buildIngredientsMapFromIngredientsCount(Map<String, Integer> ingredientsCountComplex) {
        Map<String,String> ingredientsMap = new HashMap<>();
        int index = 1;
        int secondCharIndex = -1;
        // Sorted so most common keys are lower
        List<String> sortedKeys = ingredientsCountComplex.entrySet().stream().sorted(
            (o1, o2) -> o2.getValue().compareTo(o1.getValue())
        ).map(i -> i.getKey()).collect(Collectors.toList());

        for (String i : sortedKeys) {
            if (index == BLU3_ENCODE.length) {
                secondCharIndex++;
                index = 0;
            }
            String code = secondCharIndex < 0 ? BLU3_ENCODE[index]
                    : DOUBLE_CHARACTER + BLU3_ENCODE[secondCharIndex] + BLU3_ENCODE[index];
            ingredientsMap.put(i, code);
            index++;
        }
        return ingredientsMap;
    }

    public static Map<String,String> getIngredientsMapFromHeader(String header) {
        String [] split = split(header, COLUMN_END);
        Map<String,String> map = new HashMap<>();
        for (String s : split(split[0], ROW_END)) {
            String [] ingredientSplit = split(s, MAPS_TO);
            if (ingredientSplit.length > 1) {
                map.put(s.substring(ingredientSplit[0].length() + 1), ingredientSplit[0]);
            }
        }
        return map;
    }

    public static int[] getSizesFromHeader(String header) {
        String [] split = split(header, COLUMN_END);
        if (split.length < 2) return new int[0];
        int x,y,z;
        try {
            String [] coords = split(split[1], MODIFIER);
            x = Integer.parseInt(coords[0]);
            y = Integer.parseInt(coords[1]);
            z = Integer.parseInt(coords[2]);
        } catch(Exception e) {
            return new int[0];
        }
        return new int [] {x,y,z};
    }

    public static ManipulatablePosition getDirectionalDataFromHeader(String header) {
        String [] split = split(header, COLUMN_END);
        Orientation orientation = Orientation.NORTH;
        Rotation rotation = Rotation.TOP;
        int scale = 1;
        try {
            String [] splitPerspective = split(split[2], ROW_END);
            orientation = Orientation.getOrientation(splitPerspective[0]);
            rotation = Rotation.fromCode(splitPerspective[1]);
            scale = Integer.parseInt(splitPerspective[2]);
        } catch(Exception e) {/* DO NOTHING ASSUME DEFAULTS */}
        return new ManipulatablePosition(0, 0, 0, orientation, rotation, scale);
    }

    /**
     * Finish building the header with directions and perspective. Should look like
     * > INGREDIENTS + COLUMN_END + DIRECTIONS + COLUMN_END + PERSPECTIVE DATA
     * >> DIRECTIONS = X_SIZE + MODIFIER + Y_SIZE + MODIFIER + Z_SIZE
     * >> PERSPECTIVE DATA = ORIENTATION + ROW_END + ROTATION + ROW_END + SCALE
     * 
     * @param existingHeader Ingredient list
     * @return Finished header
     */
    public static String buildHeaderWithPerspective(String existingHeader, ManipulatablePosition position) {
        StringBuilder sb = new StringBuilder();
        sb.append(existingHeader);
        sb.append(COLUMN_END);
        sb.append(position.getXSize()).append(MODIFIER).append(position.getYSize()).append(MODIFIER)
                .append(position.getZSize());
        sb.append(COLUMN_END);
        sb.append(position.getOrientation().getDescription()).append(ROW_END);
        sb.append(position.getRotation().getCode()).append(ROW_END);
        sb.append(position.getScale());
        return sb.toString();
    }

    public static String[] modifierSplit(String str) {
        return split(str, MODIFIER);
    }
    
    public static boolean isEncoded(String str) {
        for (String e : BLU3_ENCODE) {
            if (e.equals(str)) return true;
        }
        return false;
    }

    // PRIVATE METHODS

    private static String [] split(String str, String delimiter) {
        return str.split(Pattern.quote(delimiter));
    }

}
