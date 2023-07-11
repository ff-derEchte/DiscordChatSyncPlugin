package de.rubymc.discordchatsync;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class MessageBroadcaster {

    private final @NotNull TextChannel channel;


    public MessageBroadcaster(@NotNull TextChannel channel) {
        this.channel = channel;
    }

    public void broadcastMessageGlobally(String message) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Bukkit.broadcast(Component.text(message));
            channel.sendMessageEmbeds(new EmbedBuilder().setAuthor("Announcement").setDescription(message).build()).queue();
        });
    }

    public void broadCastMinecraft(String name, String message) {
        Component component = Component
                .text("[Discord] ["+name+"] ")
                .color(TextColor.fromHexString("#7289DA"))
                .append(Component.text(message).color(NamedTextColor.GRAY));

        Bukkit.broadcast(component);
    }

    public void broadCastDiscord(String name, String message) {
        try {
            //channel.sendMessageEmbeds(embed).queue();
            channel.sendMessage("**"+name+"** » "+message.replaceAll("@", "＠")).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
