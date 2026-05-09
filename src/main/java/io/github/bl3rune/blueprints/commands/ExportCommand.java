package io.github.bl3rune.blueprints.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.data.BlueprintData;
import io.github.bl3rune.blueprints.utils.InventoryUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class ExportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = InventoryUtils.getHeldBlu3print(player, false);
            if (InventoryUtils.itemIsBlank(item)) {
                sender.sendMessage("You must be holding a blueprint to export it.");
                return true;
            }

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if  (lore.size() < 2) {
                sender.sendMessage("Lore is missing from blueprint to export it.");
                return true;
            }

            sender.sendMessage(Component.text(lore.get(0), NamedTextColor.BLUE));
            BlueprintData data = Blueprints.getInstance().getBlueprintFromCache(lore.get(1));
            String encoded = data.getEncodedString();
            Component component = Component.text(encoded, NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy to clipboard")))
                    .clickEvent(ClickEvent.copyToClipboard(encoded));
            sender.sendMessage(component);

        }
        return true;
    }

}
