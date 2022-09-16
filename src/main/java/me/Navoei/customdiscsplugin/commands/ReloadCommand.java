package me.Navoei.customdiscsplugin.commands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ReloadCommand extends SubCommand {

    private final CustomDiscs plugin;

    public ReloadCommand(CustomDiscs plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Reloads the config file";
    }

    @Override
    public String getSyntax() {
        return ChatColor.GREEN + "/customdisc reload";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!player.hasPermission("customdiscs.command.reload")) {
            player.sendMessage(ChatColor.RED + "You do not have access to this command!");
            return;
        }

        plugin.reload();
    }
}
