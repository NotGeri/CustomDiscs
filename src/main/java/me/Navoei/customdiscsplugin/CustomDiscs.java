package me.Navoei.customdiscsplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import me.Navoei.customdiscsplugin.commands.CommandManager;
import me.Navoei.customdiscsplugin.events.JukeBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

public final class CustomDiscs extends JavaPlugin {

    public static final String PLUGIN_ID = "CustomDiscs";
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);

    @Nullable
    private VoicePlugin voicechatPlugin;

    private FileConfiguration config;
    public float musicDiscDistance;
    public float musicDiscVolume;

    @Override
    public void onEnable() {

        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);

        File musicData = new File(this.getDataFolder(), "musicdata");
        if (!musicData.exists()) musicData.mkdirs();

        if (service != null) {
            voicechatPlugin = new VoicePlugin();
            service.registerPlugin(voicechatPlugin);
            LOGGER.info("Successfully registered CustomDiscs plugin");
        } else {
            LOGGER.info("Failed to register CustomDiscs plugin");
        }

        HopperManager hopperManager = new HopperManager(this);
        PlayerManager playerManager = new PlayerManager(this, hopperManager);
        hopperManager.init(playerManager);

        // Register listeners
        this.getServer().getPluginManager().registerEvents(new JukeBox(this, hopperManager, playerManager), this);
        this.getServer().getPluginManager().registerEvents(hopperManager, this);

        // Register commands
        this.getCommand("customdisc").setExecutor(new CommandManager(this));

        // Get config settings
        this.reload();
        this.musicDiscDistance = config.getInt("settings.music-disc-distance");
        this.musicDiscVolume = Float.parseFloat(Objects.requireNonNull(config.getString("settings.music-disc-volume")));

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new CustomPacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.WORLD_EVENT));
    }

    @Override
    public void onDisable() {
        if (voicechatPlugin == null) return;
        getServer().getServicesManager().unregister(voicechatPlugin);
        LOGGER.info("Successfully unregistered CustomDiscs plugin");
    }

    public void reload() {
        this.saveDefaultConfig();
        this.reloadConfig();
        this.config = this.getConfig();
    }

    public FileConfiguration getConfiguration() {
        return config;
    }

    public String getTranslation(String key) {
        ConfigurationSection section = config.getConfigurationSection("translations");
        if (section == null) {
            LOGGER.info("Unable to translate text: " + key + " : configuration section 'translations' not found!");
            return "";
        }

        String translation = section.getString(key, null);
        if (translation == null) {
            LOGGER.info("Unable to translate text: " + key + " not found!");
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', translation);
    }

}