package com.mcmoddev.mmdbot.commander.reminders;

import com.mcmoddev.mmdbot.commander.TheCommander;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public record Reminder(String content, long channelId, boolean isPrivateChannel, long ownerId,
                       Instant time, AtomicBoolean removed) implements Runnable {

    public Reminder(String content, long channelId, boolean isPrivateChannel, long ownerId,
                    Instant time) {
        this(content, channelId, isPrivateChannel, ownerId, time, new AtomicBoolean());
    }

    public static final Collection<Message.MentionType> ALLOWED_MENTIONS = EnumSet.of(
        Message.MentionType.EMOTE, Message.MentionType.USER, Message.MentionType.CHANNEL
    );
    public static final Color COLOUR = Color.LIGHT_GRAY;

    @Override
    public void run() {
        if (removed.get()) return;
        final var jda = TheCommander.getJDA();
        if (jda == null) {
            log.warn("Could not run reminder due to JDA instance being null.");
            return;
        }
        final var user = jda.getUserById(ownerId);
        if (user == null) {
            log.warn("Could not find user with ID {} for a reminder.", ownerId);
            return;
        }
        final MessageChannel channel = io.github.matyrobbrt.curseforgeapi.util.Utils.makeWithSupplier(() -> {
           if (isPrivateChannel()) {
               return jda.getPrivateChannelById(channelId);
           } else {
               final var ch = jda.getChannelById(MessageChannel.class, channelId);
               if (ch == null) {
                   return jda.getThreadChannelById(channelId);
               }
               return ch;
           }
        });
        if (channel == null) {
            log.warn("Could not find channel with ID {} for reminder.", channelId);
            return;
        }
        final var canTalk = channel.canTalk();
        if (!canTalk) {
            log.warn("Could not talk in channel with ID {}, so a reminder could not be sent!", channelId);
            return;
        }
        channel.sendMessage(new MessageBuilder()
                .setContent(user.getAsMention())
                .setEmbeds(
                    new EmbedBuilder()
                        .setAuthor(jda.getSelfUser().getName(), null, jda.getSelfUser().getAvatarUrl())
                        .setTitle("Reminder")
                        .setFooter(user.getName(), user.getAvatarUrl())
                        .setDescription(content)
                        .setTimestamp(Instant.now())
                        .setColor(COLOUR)
                        .build()
                )
                .setAllowedMentions(ALLOWED_MENTIONS)
                .build())
            .queue(v -> {}, error -> log.error("Exception while trying to send reminder!", error));
    }
}
