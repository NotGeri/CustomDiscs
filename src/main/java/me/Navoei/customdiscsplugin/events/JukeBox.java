package me.Navoei.customdiscsplugin.events;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.HopperManager;
import me.Navoei.customdiscsplugin.PlayerManager;
import me.Navoei.customdiscsplugin.VoicePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class JukeBox implements Listener {

    private final CustomDiscs plugin;
    private final HopperManager hopperManager;
    private final PlayerManager playerManager;

    public JukeBox(CustomDiscs plugin, HopperManager hopperManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
        this.playerManager = playerManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInsert(PlayerInteractEvent event) throws IOException {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || event.getItem() == null || event.getItem().getItemMeta() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (isCustomMusicDisc(event) && !jukeboxContainsDisc(block)) {

            String soundFileName = event.getItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "customdisc"), PersistentDataType.STRING);

            Path soundFilePath = Path.of(plugin.getDataFolder().getPath(), "musicdata", soundFileName);

            if (soundFilePath.toFile().exists()) {

                assert VoicePlugin.voicechatServerApi != null;
                playerManager.playLocationalAudio(VoicePlugin.voicechatServerApi, soundFilePath, player, block);

                Component songNameComponent = Objects.requireNonNull(event.getItem().getItemMeta().lore()).get(0).asComponent();
                String songName = PlainTextComponentSerializer.plainText().serialize(songNameComponent);

                TextComponent customActionBarSongPlaying = Component.text()
                        .content("Now Playing: " + songName)
                        .color(NamedTextColor.GOLD)
                        .build();
                player.sendActionBar(customActionBarSongPlaying.asComponent());

            } else {
                player.sendMessage(ChatColor.RED + "Sound file not found.");
                event.setCancelled(true);
                throw new FileNotFoundException("Sound file is missing!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEject(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null || block == null) return;
        if (event.getClickedBlock().getType() != Material.JUKEBOX) return;

        if (jukeboxContainsDisc(block)) {
            stopDisc(block, player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> hopperManager.getNextDiscFromHopperIntoJukebox(block), 1L);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxBreak(BlockBreakEvent event) {

        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getType() != Material.JUKEBOX) return;

        stopDisc(block, player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onJukeboxExplode(EntityExplodeEvent event) {

        for (Block explodedBlock : event.blockList()) {
            if (explodedBlock.getType() == Material.JUKEBOX) {
                stopDisc(explodedBlock, null);
            }
        }

    }

    public boolean jukeboxContainsDisc(Block b) {
        Jukebox jukebox = (Jukebox) b.getLocation().getBlock().getState();
        return jukebox.getPlaying() != Material.AIR;
    }

    public boolean isCustomMusicDisc(PlayerInteractEvent e) {

        if (e.getItem() == null) return false;

        return e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "customdisc")) &&
                (e.getItem().getType().equals(Material.MUSIC_DISC_13) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_CAT) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_BLOCKS) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_CHIRP) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_FAR) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_MALL) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_MELLOHI) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_STAL) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_STRAD) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_WARD) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_11) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_WAIT) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_OTHERSIDE) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_5) ||
                        e.getItem().getType().equals(Material.MUSIC_DISC_PIGSTEP)
                );
    }

    private void stopDisc(Block block, Player player) {
        playerManager.stopLocationalAudio(block.getLocation());
        if (player == null) return;
        if (jukeboxContainsDisc(block)) {
            player.swingMainHand();
        }
    }

}
