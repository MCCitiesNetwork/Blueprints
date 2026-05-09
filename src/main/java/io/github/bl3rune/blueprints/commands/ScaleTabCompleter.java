package io.github.bl3rune.blueprints.commands;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.github.bl3rune.blueprints.config.GlobalConfig;

public class ScaleTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        
        if (args.length == 1) {
            Integer max = GlobalConfig.getMaxScale();
            return IntStream.rangeClosed(1, max == null ? 10 : max).boxed()
                .map(i -> "" + i).collect(Collectors.toList());
        }

        return null;
    }

}
