package de.rubymc.discordchatsync;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private JDA jda;
    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        Main.instance = this;

        FileConfiguration config = getConfigSave();

        if (config == null) {
            return;
        }

        String token = config.getString("token");
        String channelId = config.getString("channelId");

        if (token == null || channelId == null) {
            getLogger().warning("Config values are missing (Shutting down)");
            exit();
        }

        initJDA(token, channelId);
    }

    private FileConfiguration getConfigSave() {
        Path configFilePath = Path.of(getDataFolder().getPath()+"/config.yml");

        if (!Files.exists(configFilePath)) {
            try {
                Files.writeString(configFilePath, """
                        #DiscordMinecraftChatSync
                        #Enter bot credentials:
                 
                        token: "<DiscordToken>"
                        channelId: "<IdAsString>"
    
                        """);
                getLogger().warning("Default Config File created. Please enter the discord token and channel id");
            } catch (IOException e) {
                getLogger().log(Level.WARNING, "Cannot write defaultConfig");
                exit();
                return null;
            }
        }

        FileConfiguration config = getConfig();
        try {
            config.load(new File(getDataFolder().getPath()+"/config.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            getLogger().warning("Cannot load config");
            exit();
            return null;
        }

        return config;
    }


    private void initJDA(String token, String channelId) {
        try {
            this.jda = JDABuilder
                    .create(token, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
                    .build();
            jda.awaitReady();
            TextChannel channel = jda.getTextChannelById(channelId);

            if (channel == null) {
                getLogger().log(Level.WARNING, "Discord channel not found");
                exit();
                return;
            }

            MessageBroadcaster broadcaster = new MessageBroadcaster(channel);
            new MessageListener(channel.getId(), broadcaster, jda);

        } catch (InvalidTokenException e) {
            getLogger().log(Level.WARNING, "Invalid discord token (shutting down)");
            exit();
        } catch (NumberFormatException | InterruptedException e) {
            getLogger().log(Level.WARNING, "Discord Error");
            exit();
        }
    }

    private void exit() {
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void onDisable() {
        if (jda != null) {
            jda.shutdownNow();
        }
        try {
            getConfig().save(new File(getDataFolder().getPath()+"/config.yml"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to save Config!");
        }
    }
}
