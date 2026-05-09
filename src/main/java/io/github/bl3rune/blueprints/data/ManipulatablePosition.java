package io.github.bl3rune.blueprints.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.github.bl3rune.blueprints.enums.Direction;
import io.github.bl3rune.blueprints.enums.Orientation;
import io.github.bl3rune.blueprints.enums.Rotation;
import io.github.bl3rune.blueprints.enums.Turn;
import io.github.bl3rune.blueprints.utils.Pair;

public class ManipulatablePosition {

    private int xMax;
    private int yMax;
    private int zMax;
    private Orientation orientation;
    private Rotation rotation;
    private int scale;

    // Temporary fields for iterations
    private Iterator<Integer> outerLoop;
    private Iterator<Integer> middleLoop;
    private Iterator<Integer> innerLoop;
    private int outerIndex;
    private int middleIndex;
    private int innerIndex;
    private boolean usingScaling;
    private Direction outer;
    private Direction middle;
    private Direction inner;

    public ManipulatablePosition(ManipulatablePosition p, int scale) {
        this(p.getZSize(), p.getYSize(), p.getXSize(), p.getOrientation(), p.getRotation(), scale);
    }

    public ManipulatablePosition(int z, int y, int x, Orientation o, Rotation r) {
        this(z, y, x, o, r, 1);
    }

    public ManipulatablePosition(int z, int y, int x, Orientation o, Rotation r, int scale) {
        this.xMax = x;
        this.yMax = y;
        this.zMax = z;
        this.orientation = o;
        this.rotation = r;
        this.scale = scale;
        this.outer = o.getOuter();
        this.middle = o.getMiddle(r);
        this.inner = o.getInner(r);
    }

    public int getXSize() {
        return xMax;
    }

    public int getYSize() {
        return yMax;
    }

    public int getZSize() {
        return zMax;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public int getScale() {
        return scale;
    }

    public int getScalingIngredientsMultiplier() {
        return scale * scale * scale;
    }

    // CONVERSION METHODS

    public int[] getNewSizes(Rotation newRotation) {
        return getNewSizes(newRotation, new int[] { zMax, yMax, xMax });
    }

    public int[] getNewSizes(Rotation newRotation, int[] sizes) {
        if (newRotation == rotation || newRotation == rotation.getOpposite()) {
            return sizes;
        }
        int outer = sizes[this.outer.getPosIndex()];
        int middle = sizes[this.middle.getPosIndex()];
        int inner = sizes[this.inner.getPosIndex()];
        return unscramble(outer, inner, middle);
    }

    public int[] getNewSizes(Orientation newOrientation) {
        int[] newSizes = new int[3];
        if (newOrientation == orientation || newOrientation == orientation.getOpposite()) {
            return new int[] { zMax, yMax, xMax };
        }
        switch (orientation) {
            default:
            case NORTH:
            case SOUTH:
            case EAST:
            case WEST:
                if (newOrientation == Orientation.UP || newOrientation == Orientation.DOWN) {
                    newSizes[0] = yMax;
                    if (newOrientation == Orientation.EAST || newOrientation == Orientation.WEST) {
                        newSizes[1] = xMax;
                        newSizes[2] = zMax;
                    } else {
                        newSizes[1] = zMax;
                        newSizes[2] = xMax;
                    }
                } else {
                    newSizes[0] = xMax;
                    newSizes[1] = yMax;
                    newSizes[2] = zMax;
                }
                break;
            case UP:
            case DOWN:
                newSizes[1] = zMax;
                if (newOrientation == Orientation.EAST || newOrientation == Orientation.WEST) {
                    newSizes[0] = xMax;
                    newSizes[2] = yMax;
                } else {
                    newSizes[0] = yMax;
                    newSizes[2] = xMax;
                }
                break;
        }

        return newSizes;
    }

    public Pair<Orientation, Rotation> calculateTurn(Turn turn) {
        switch (orientation) {
            default:
            case NORTH:
                return calculatePossibleNewOrientations(new Orientation[] { // UP, RIGHT, DOWN, LEFT
                        Orientation.DOWN,
                        Orientation.EAST,
                        Orientation.UP,
                        Orientation.WEST
                }, turn);
            case SOUTH:
                return calculatePossibleNewOrientations(new Orientation[] {
                        Orientation.DOWN,
                        Orientation.WEST,
                        Orientation.UP,
                        Orientation.EAST
                }, turn);
            case EAST:
                return calculatePossibleNewOrientations(new Orientation[] {
                        Orientation.DOWN,
                        Orientation.SOUTH,
                        Orientation.UP,
                        Orientation.NORTH
                }, turn);
            case WEST:
                return calculatePossibleNewOrientations(new Orientation[] {
                        Orientation.DOWN,
                        Orientation.NORTH,
                        Orientation.UP,
                        Orientation.SOUTH
                }, turn);
            case UP:
                return calculatePossibleNewOrientations(new Orientation[] {
                        Orientation.NORTH,
                        Orientation.EAST,
                        Orientation.SOUTH,
                        Orientation.WEST
                }, turn);
            case DOWN:
                return calculatePossibleNewOrientations(new Orientation[] {
                        Orientation.NORTH,
                        Orientation.WEST,
                        Orientation.SOUTH,
                        Orientation.EAST
                }, turn);
        }
    }

    /**
     * 
     * @param newOrientations New Orientations when turning from UP, RIGHT, DOWN,
     *                        LEFT in the TOP rotaiton for each side
     * @param turn            Which direction to turn
     * @return
     */
    private Pair<Orientation, Rotation> calculatePossibleNewOrientations(Orientation[] newOrientations, Turn turn) {
        turn = turn.plusRotation(rotation);
        Orientation o = newOrientations[turn.getIndex()];
        Rotation r = null;

        if (o.isCompass() && orientation.isCompass()) {
            r = rotation;
        } else {
            Orientation other = o.isCompass() ? o : orientation;
            boolean isUp = o == Orientation.UP || orientation == Orientation.UP;
            boolean turningUp = o == Orientation.UP || orientation == Orientation.DOWN;
            switch (other) {
                default: // NORTH
                    r = isUp ? rotation : rotation.getOpposite();
                    break;
                case EAST:
                    r = turningUp ? rotation.getNextRotation() : Rotation.getRotation(rotation.getIndex() + 3);
                    break;
                case SOUTH:
                    r = isUp ? rotation.getOpposite() : rotation;
                    break;
                case WEST:
                    r = turningUp ? Rotation.getRotation(rotation.getIndex() + 3) : rotation.getNextRotation();
                    break;
            }
        }

        return new Pair<>(o, r);
    }

    // ITERATION METHODS

    public boolean endOfMiddleLoop() {
        return middleLoop != null && !middleLoop.hasNext();
    }

    public boolean endOfInnerLoop() {
        return innerLoop != null && !innerLoop.hasNext();
    }

    /**
     * Gets the next position in the 3D sequence based on the iterators
     * - outerLoop
     * - middleLoop
     * - innerLoop
     * 
     * @return the next position in the 3D sequence.
     */
    public int[] next(boolean scaling) {
        if (outerLoop == null || usingScaling != scaling) {
            resetLoops(scaling);
        }

        if (!innerLoop.hasNext()) {
            if (!middleLoop.hasNext()) {
                if (!outerLoop.hasNext()) {
                    outerLoop = null;
                    return null;
                }
                outerIndex = outerLoop.next();
                middleLoop = getLoop(middle, (scaling ? scale : 1));
            }
            middleIndex = middleLoop.next();
            innerLoop = getLoop(inner, (scaling ? scale : 1));
        }
        innerIndex = innerLoop.next();
        return unscramble(outerIndex, middleIndex, innerIndex);
    }

    /**
     * Resets all iterators back to the beginning
     * 
     * @param scaling should loops be scaled by `this.scale`
     */
    private void resetLoops(boolean scaling) {
        usingScaling = scaling;
        outerLoop = getLoop(outer, (scaling ? scale : 1));
        middleLoop = getLoop(middle, (scaling ? scale : 1));
        innerLoop = getLoop(inner, (scaling ? scale : 1));
        outerIndex = outerLoop.next();
        middleIndex = middleLoop.next();
    }

    /**
     * Builds an iterator of integers based on the size of direction and the scaling
     * factor
     * 
     * @param direction Direction of the integers along a particular axis
     * @param scaling   scaling factor to multiply the maximum distance in a
     *                  direction by
     * @return Iterator from (start of direction to end of direction, multiplied by
     *         the scaling factor)
     */
    private Iterator<Integer> getLoop(Direction direction, int scaling) {
        int loopEnd = 0;
        switch (direction) {
            case X_POS:
            case X_NEG:
                loopEnd = xMax * scaling;
                break;
            case Y_POS:
            case Y_NEG:
                loopEnd = yMax * scaling;
                break;
            case Z_POS:
            case Z_NEG:
            default:
                loopEnd = zMax * scaling;
                break;
        }
        List<Integer> list = IntStream.range(0, loopEnd).boxed().collect(Collectors.toList());
        if (direction.isNegative()) {
            Collections.reverse(list);
        }
        return list.iterator();
    }

    /**
     * Unscrambles values using ordering.
     * 
     * @param outer  the outer value.
     * @param middle the middle value.
     * @param inner  the inner value.
     * @return the unscrambled values in [z][y][x] order.
     */
    private int[] unscramble(int outer, int middle, int inner) {
        int[] result = new int[3];
        result[this.outer.getPosIndex()] = outer;
        result[this.middle.getPosIndex()] = middle;
        result[this.inner.getPosIndex()] = inner;
        return result;
    }
}
