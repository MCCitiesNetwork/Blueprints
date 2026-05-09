package io.github.bl3rune.blueprints.services.domain;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import io.github.bl3rune.blueprints.config.GlobalConfig;
import io.github.bl3rune.blueprints.data.ManipulatablePosition;

/**
 * Validates per-player size/scale permissions and limits previously inlined
 * on {@code BlueprintData}. Stateless. Behavior is preserved exactly: same
 * permission keys, same messages, same null-tolerant short-circuiting.
 */
public final class LimitValidator {

    public boolean playerAllowedToUse(Player player, ManipulatablePosition position) {
        int scale = position.getScale();
        int[] sizes = new int[] { position.getXSize(), position.getYSize(), position.getZSize() };

        Integer maxSize = GlobalConfig.getMaxSize();
        if (player != null && maxSize != null && sizesExceedLimit(sizes, 1, maxSize)) {
            if (!player.hasPermission("blu3print.no-size-limit")) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to set size over the max size limit of " + maxSize + "!");
                return false;
            }
        }

        Integer maxScale = GlobalConfig.getMaxScale();
        if (player != null && maxScale != null && scale > maxScale) {
            if (!player.hasPermission("blu3print.no-scale-limit")) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to increase scale over the max scale limit of " + maxScale
                        + "!");
                return false;
            }
        }

        Integer maxOverallSize = GlobalConfig.getMaxOverallSize();
        if (player != null && maxOverallSize != null && sizesExceedLimit(sizes, scale, maxOverallSize)) {
            if (!player.hasPermission("blu3print.no-scale-limit")
                    && !player.hasPermission("blu3print.no-size-limit")) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to increase size over the max overall size limit of "
                        + maxOverallSize + "!");
                return false;
            }
        }

        return true;
    }

    public boolean newScaleAllowed(Player player, ManipulatablePosition position, int newScale) {
        Integer maxScale = GlobalConfig.getMaxScale();
        if (player != null && maxScale != null && newScale > maxScale) {
            if (!player.hasPermission("blu3print.no-scale-limit")) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to increase scale over the max scale limit of " + maxScale
                        + "!");
                return false;
            }
        }

        Integer maxOverallSize = GlobalConfig.getMaxOverallSize();
        if (player != null && maxOverallSize != null
                && ((position.getXSize() * newScale) > maxOverallSize
                        || (position.getYSize() * newScale) > maxOverallSize
                        || (position.getZSize() * newScale) > maxOverallSize)) {
            if (!player.hasPermission("blu3print.no-scale-limit")
                    && !player.hasPermission("blu3print.no-size-limit")) {
                player.sendMessage(ChatColor.RED
                        + "You do not have permission to increase size over the max overall size limit of "
                        + maxOverallSize + "!");
                return false;
            }
        }
        return true;
    }

    public boolean sizesExceedLimit(int[] sizes, int scale, int max) {
        for (int size : sizes) {
            if (size * scale > max) {
                return true;
            }
        }
        return false;
    }
}
