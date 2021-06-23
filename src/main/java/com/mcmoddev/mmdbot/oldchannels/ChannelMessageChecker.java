package com.mcmoddev.mmdbot.oldchannels;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.entities.Guild;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class ChannelMessageChecker extends TimerTask {

    private final long guildId;
    private final Guild guild;

    public ChannelMessageChecker() {
        this.guildId = MMDBot.getConfig().getGuildID();
        this.guild = MMDBot.getInstance().getGuildById(MMDBot.getConfig().getGuildID());
    }

    @Override
    public void run() {
        if (guild == null) {
            MMDBot.LOGGER.error("Error while checking for old channels: guild {} doesn't exist!", guildId);
            return;
        }
        OldChannelsHelper.clear();

        final Instant currentTime = Instant.now();

        CompletableFuture.allOf(guild.getTextChannels()
            .parallelStream()
            .map(channel -> channel.getIterableHistory()
                .takeAsync(100).thenAcceptAsync(
                    messages -> messages.stream().filter(message -> !message.isWebhookMessage()).findFirst().ifPresent(message -> {
                        final long daysSinceLastMessage = ChronoUnit.DAYS.between(message.getTimeCreated().toInstant(), currentTime);
                        OldChannelsHelper.put(message.getTextChannel(), daysSinceLastMessage);
                    })
                )).toArray(CompletableFuture[]::new))
            .thenAccept(v -> OldChannelsHelper.setReady(true));
    }
}
