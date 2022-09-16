package me.Navoei.customdiscsplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Jukebox;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class CustomPacketAdapter extends PacketAdapter {

    private final Plugin plugin;
    public CustomPacketAdapter(Plugin plugin, ListenerPriority listenerPriority, PacketType... types) {
        super(plugin, listenerPriority, types);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();

        if (packet.getIntegers().read(0).toString().equals("1010")) {
            Jukebox jukebox = (Jukebox) packet.getBlockPositionModifier().read(0).toLocation(event.getPlayer().getWorld()).getBlock().getState();

            if (!jukebox.getRecord().hasItemMeta()) return;

            if (jukebox.getRecord().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "customdisc"), PersistentDataType.STRING)) {
                event.setCancelled(true);
            }

        }
    }

}
