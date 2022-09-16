package me.Navoei.customdiscsplugin.commands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CreateCommand extends SubCommand {

    private final CustomDiscs plugin;

    public CreateCommand(CustomDiscs plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return ChatColor.GRAY + "Creates a custom music disc.";
    }

    @Override
    public String getSyntax() {
        return ChatColor.GREEN + "/customdisc create <filename> \"Custom Lore\"";
    }

    @Override
    public void perform(Player player, String[] args) {
        if (!isMusicDisc(player)) {
            player.sendMessage(ChatColor.RED + "You are not holding a music disc in your main hand!");
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Insufficient arguments! ( /customdisc create <filename> \"Custom Lore\" )");
            return;
        }

        if (!player.hasPermission("customdiscs.create")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }

        // /cd create test.mp3 "test"
        //      [0]     [1]     [2]
        //Find file, if file not there then say "file not there"
        String filename = args[1];

        if (customName(readQuotes(args)).equalsIgnoreCase("")) {
            player.sendMessage(ChatColor.RED + "You must provide a name for your disc.");
            return;
        }

        if (filename.startsWith("http")) {
            String downloadedFile = filename.substring(filename.lastIndexOf('/') + 1);

            if (!getFileExtension(downloadedFile).equals("wav") && !getFileExtension(downloadedFile).equals("mp3")) {
                player.sendMessage(ChatColor.RED + "Invalid download extension");
                return;
            }

            player.sendMessage("Loading...");
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

                try {
                    URL url = new URL(filename);

                    URLConnection connection = url.openConnection();

                    if (connection != null) {
                        long size = connection.getContentLengthLong() / 1048576;
                        if (size > plugin.getConfiguration().getInt("settings.music-disc-max-download-size", 50)) {
                            player.sendMessage(ChatColor.RED + "Too big man yes");
                            return;
                        }
                    }

                    BufferedInputStream is = new BufferedInputStream(url.openStream());
                    Files.copy(is, Paths.get(plugin.getDataFolder().getAbsolutePath(), "musicdata", downloadedFile));

                } catch (IOException exception) {
                    player.sendMessage(ChatColor.RED + "An error occurred: " + exception.getMessage());
                    return;
                }

                this.processFile(player, downloadedFile, args);
            });

        }

        this.processFile(player, filename, args);
    }

    private void processFile(Player player, String fileName, String[] args) {
        File getDirectory = new File(plugin.getDataFolder(), "musicdata");
        File songFile = new File(getDirectory.getPath(), fileName);
        if (songFile.exists()) {
            if (!getFileExtension(fileName).equals("wav") && !getFileExtension(fileName).equals("mp3")) {
                player.sendMessage(ChatColor.RED + "File is not in wav or mp3 format!");
                return;
            }
        } else {
            player.sendMessage(ChatColor.RED + "File not found!");
            return;
        }

        // Sets the lore of the item to the quotes from the command.
        ItemStack disc = new ItemStack(player.getInventory().getItemInMainHand());
        ItemMeta meta = disc.getItemMeta();
        @Nullable List<Component> itemLore = new ArrayList<>();
        final TextComponent customLoreSong = Component.text()
                .decoration(TextDecoration.ITALIC, false)
                .content(customName(readQuotes(args)))
                .color(NamedTextColor.GRAY)
                .build();
        itemLore.add(customLoreSong);
        meta.addItemFlags(ItemFlag.values());
        meta.lore(itemLore);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(new NamespacedKey(plugin, "customdisc"), PersistentDataType.STRING, fileName);

        player.getInventory().getItemInMainHand().setItemMeta(meta);

        // Todo (Geri): plugin.getTranslation("your-file-name")
        player.sendMessage("Your filename is: " + ChatColor.GRAY + fileName);
        player.sendMessage("Your custom name is: " + ChatColor.GRAY + customName(readQuotes(args)));
    }

    private String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }

    private ArrayList<String> readQuotes(String[] args) {
        ArrayList<String> quotes = new ArrayList<>();
        String temp = "";
        boolean inQuotes = false;

        for (String s : args) {
            if (s.startsWith("\"") && s.endsWith("\"")) {
                temp += s.substring(1, s.length() - 1);
                quotes.add(temp);
            } else if (s.startsWith("\"")) {
                temp += s.substring(1);
                quotes.add(temp);
                inQuotes = true;
            } else if (s.endsWith("\"")) {
                temp += s.substring(0, s.length() - 1);
                quotes.add(temp);
                inQuotes = false;
            } else if (inQuotes) {
                temp += s;
                quotes.add(temp);
            }
            temp = "";
        }

        return quotes;
    }

    private String customName(ArrayList<String> q) {

        StringBuffer sb = new StringBuffer();

        for (String s : q) {
            sb.append(s);
            sb.append(" ");
        }

        if (sb.isEmpty()) {
            return sb.toString();
        } else {
            return sb.toString().substring(0, sb.length() - 1);
        }
    }

    private boolean isMusicDisc(Player p) {

        return p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_13) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_CAT) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_CHIRP) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_FAR) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_MALL) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_STAL) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_STRAD) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_WARD) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_11) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_WAIT) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_5) ||
                p.getInventory().getItemInMainHand().getType().equals(Material.MUSIC_DISC_PIGSTEP);
    }

}
