package io.github.bl3rune.blueprints.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.utils.InventoryUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ExportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack item = InventoryUtils.getHeldBlu3print(player, false);
            if (InventoryUtils.itemIsBlank(item)) {
                sender.sendMessage("You must be holding a blu3print to export it.");
                return true;
            }

            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if  (lore.size() < 2) {
                sender.sendMessage("Lore is missing from blu3print to export it.");
                return true;
            }

            sender.sendMessage(ChatColor.BLUE + lore.get(0));
            Blu3printData data = Blueprints.getInstance().getBlu3printFrpmCache(lore.get(1));
            BaseComponent component = new TextComponent(ChatColor.GRAY + data.getEncodedString());
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy to clipboard")));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, data.getEncodedString()));
            sender.spigot().sendMessage(component);
            
        }
        return true;
    }

}
