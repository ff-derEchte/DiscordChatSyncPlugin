package de.rubymc.discordchatsync;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class MessageListener extends ListenerAdapter implements Listener {

    private final String channelId;

    private final MessageBroadcaster broadcaster;
    private final JDA jda;

    public MessageListener(String channelId, MessageBroadcaster broadcaster, JDA jda) {
        this.channelId = channelId;
        this.broadcaster = broadcaster;
        this.jda = jda;

        jda.addEventListener(this);
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());

    }

    @EventHandler
    public void onAsyncChatEvent(AsyncPlayerChatEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            broadcaster.broadCastDiscord(event.getPlayer().getName(), event.getMessage());
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {

        if (event.getAuthor().getId().equals(jda.getSelfUser().getId())) {
            return;
        }

        if (!event.getChannel().getId().equals(channelId)) {
            return;
        }

        broadcaster.broadCastMinecraft(
                event.getAuthor().getEffectiveName(),
                event.getMessage().getContentDisplay()
        );

    }
}
