package me.Navoei.customdiscsplugin.commands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final ArrayList<SubCommand> subCommands = new ArrayList<>();

    public CommandManager(CustomDiscs plugin) {
        subCommands.add(new CreateCommand(plugin));
        subCommands.add(new ConvertCommand(plugin));
        subCommands.add(new ReloadCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        if (args.length > 0) {
            for (int i = 0; i < getSubCommands().size(); i++) {
                if (args[0].equalsIgnoreCase(getSubCommands().get(i).getName())) {
                    getSubCommands().get(i).perform(player, args);
                }
            }
        } else {
            player.sendMessage(ChatColor.AQUA + "----[ Custom Discs ]----");
            for (int i = 0; i < getSubCommands().size(); i++) {
                player.sendMessage(getSubCommands().get(i).getSyntax() + ChatColor.DARK_GRAY + " - " + getSubCommands().get(i).getDescription());
            }
            player.sendMessage(ChatColor.AQUA + "---------------------");
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 1) {
            List<String> arguments = new ArrayList<>();
            for (int i = 0; i < getSubCommands().size(); i++) {
                arguments.add(getSubCommands().get(i).getName());
            }
            return arguments;
        }

        return null;
    }

    public ArrayList<SubCommand> getSubCommands() {
        return subCommands;
    }

}
