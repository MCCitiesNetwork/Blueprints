package io.github.bl3rune.blueprints.commands;

import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import io.github.bl3rune.blueprints.Blueprints;
import io.github.bl3rune.blueprints.data.Blu3printData;
import io.github.bl3rune.blueprints.data.ImportedBlu3printData;
import io.github.bl3rune.blueprints.items.Blu3printItem;
import io.github.bl3rune.blueprints.utils.InventoryUtils;

import org.bukkit.inventory.ItemStack;
// import org.bukkit.inventory.meta.BookMeta;
// import org.bukkit.inventory.meta.WritableBookMeta;

public class ImportCommand implements CommandExecutor {

    private final Blueprints instance;

    public ImportCommand(Blueprints instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Inventory inventory = player.getInventory();
            if (args.length < 2) {
                sender.sendMessage("You must provide a name and a blu3print string to import.");
                return false;
            }
            if (args.length > 2) {
                sender.sendMessage("Too many arguments.");
                return false;
            }
            ItemStack blankBlu3print = InventoryUtils.getHeldBlu3print(player, true);
            if (blankBlu3print == null) {
                sender.sendMessage("You must be holding a blank blu3print to import a blu3print.");
                return false;
            }
            String uuid = instance.getKeyFromEncoding(args[1]);
            if (uuid == null) {
                uuid = UUID.randomUUID().toString();
            }
            Blu3printItem blu3print = Blu3printItem.getFinishedBlu3print(uuid, "imported by" + player.getDisplayName(), args[0], true);
            Blu3printData blu3printData = new ImportedBlu3printData(player, args[1], uuid);
            if (blu3printData.getPosition() == null) return true;
            instance.saveOrUpdateCachedBlu3print(uuid, blu3printData);

            inventory.addItem(blu3print);
            sender.sendMessage("Importing blu3print...");
        } else {
            sender.sendMessage("You must be a player to use this command.");
        }
        return true;
    }

}
