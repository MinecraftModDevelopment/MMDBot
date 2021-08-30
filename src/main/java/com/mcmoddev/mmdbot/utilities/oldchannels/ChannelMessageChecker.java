package com.mcmoddev.mmdbot.utilities.oldchannels;

import com.mcmoddev.mmdbot.MMDBot;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

/**
 * The type Channel message checker.
 *
 * @author williambl
 */
public class ChannelMessageChecker extends TimerTask {

    /**
     * The Guild id.
     */
    private final long guildId;
    /**
     * The Guild.
     */
    private final Guild guild;

    /**
     * Instantiates a new Channel message checker.
     */
    public ChannelMessageChecker() {
        this.guildId = MMDBot.getConfig().getGuildID();
        this.guild = MMDBot.getInstance().getGuildById(MMDBot.getConfig().getGuildID());
    }

    /**
     * Run.
     */
    @Override
    public void run() {
        if (guild == null) {
            MMDBot.LOGGER.error("Error while checking for old channels: guild {} doesn't exist!", guildId);
            return;
        }
        OldChannelsHelper.clear();

        final var currentTime = Instant.now();
        final Member self = guild.getSelfMember();

        CompletableFuture.allOf(guild.getTextChannelCache()
                .parallelStreamUnordered()
                .filter(self::hasAccess)
                .filter(channel -> self.hasPermission(channel, Permission.MESSAGE_HISTORY))
                .map(channel -> channel.getIterableHistory()
                    .takeAsync(1000).thenAccept(messages -> messages.stream()
                        .filter(message -> !message.isWebhookMessage() && !message.getType().isSystem())
                        .findFirst()
                        .ifPresent(message -> {
                            final long daysSinceLastMessage = ChronoUnit.DAYS.between(message.getTimeCreated()
                                .toInstant(), currentTime);
                            OldChannelsHelper.put(message.getTextChannel(), daysSinceLastMessage);
                        })
                    )).toArray(CompletableFuture[]::new))
            .thenAccept(v -> OldChannelsHelper.setReady(true));
    }
}
