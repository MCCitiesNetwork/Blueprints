package io.github.bl3rune.blueprints.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class GiveTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(
                    p -> p.getName()
                ).collect(Collectors.toList());
        }

        return null;
    }

}
