package io.github.bl3rune.blueprints.services;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.bl3rune.blueprints.items.BlueprintItem;

/**
 * Registers the Blu3print Writer crafting recipe. The ingredient list is read
 * from the new {@code blueprints.recipe.ingredients} key with a fallback to
 * the legacy {@code blu3print.recipe.ingredients} so existing operator
 * configs keep working through the migration window.
 */
public final class RecipeRegistrar {

    private static final String NEW_KEY = "blueprints.recipe.ingredients";
    private static final String LEGACY_KEY = "blu3print.recipe.ingredients";

    public void register(JavaPlugin plugin) {
        ShapelessRecipe recipe = new ShapelessRecipe(
                new NamespacedKey(plugin, "Blu3print_Writer"),
                BlueprintItem.getBlankBlu3print());
        recipe.setGroup("Tools & Utilities");

        String recipeKey = plugin.getConfig().contains(NEW_KEY) ? NEW_KEY : LEGACY_KEY;
        List<String> ingredients = plugin.getConfig().getStringList(recipeKey);

        try {
            for (String i : ingredients) {
                recipe.addIngredient(Material.valueOf(i));
            }
            Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid blu3print recipe. using default recipe");
            ShapelessRecipe fallback = new ShapelessRecipe(
                    new NamespacedKey(plugin, "Blu3print_Writer"),
                    BlueprintItem.getBlankBlu3print());
            fallback.addIngredient(Material.PAPER);
            fallback.addIngredient(Material.LAPIS_LAZULI);
            fallback.addIngredient(Material.FEATHER);
            Bukkit.addRecipe(fallback);
        }
    }
}
