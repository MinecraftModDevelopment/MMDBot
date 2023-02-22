/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2023 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.commander.reminders;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public record Reminder(String content, long channelId, boolean isPrivateChannel, long ownerId,
                       Instant time, AtomicBoolean removed) implements Runnable {

    public Reminder(String content, long channelId, boolean isPrivateChannel, long ownerId,
                    Instant time) {
        this(content, channelId, isPrivateChannel, ownerId, time, new AtomicBoolean());
    }

    public static final Collection<Message.MentionType> ALLOWED_MENTIONS = EnumSet.of(
        Message.MentionType.EMOJI, Message.MentionType.USER, Message.MentionType.CHANNEL
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
        if (!TheCommander.getInstance().getGeneralConfig().features().reminders().areEnabled()) {
            return;
        }
        jda.retrieveUserById(ownerId).queue(user -> {
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
                if (!isPrivateChannel()) {
                    // If we can't DM the user, then log
                    user.openPrivateChannel().queue(pv -> {
                        pv.sendMessage(buildMessage(jda, user)
                                .addContent(System.lineSeparator())
                                .addContent("*Could not send reminder in <#%s>.*".formatted(channelId()))
                                .build())
                            .queue(m -> SnoozingListener.INSTANCE.addSnoozeListener(m.getIdLong(), this),
                                error -> log.error("Exception while trying to send reminder!", error));
                    }, e -> log.warn("Could not talk in channel with ID {}, so a reminder could not be sent!", channelId));
                } else {
                    log.warn("Could not talk in channel with ID {}, so a reminder could not be sent!", channelId);
                }
                return;
            }
            channel.sendMessage(buildMessage(jda, user).build())
                .queue(m -> SnoozingListener.INSTANCE.addSnoozeListener(m.getIdLong(), this),
                    error -> log.error("Exception while trying to send reminder!", error));
        }, $ -> log.warn("Could not find user with ID {} for a reminder.", ownerId));
    }

    public MessageCreateBuilder buildMessage(final JDA jda, final User user) {
        return new MessageCreateBuilder()
            .mention(user)
            .setContent(isPrivateChannel() ? null : user.getAsMention())
            .setEmbeds(
                new EmbedBuilder()
                    .setAuthor(jda.getSelfUser().getName(), null, jda.getSelfUser().getAvatarUrl())
                    .setTitle("Reminder")
                    .setFooter(user.getName(), user.getAvatarUrl())
                    .setDescription(content.isBlank() ? "No Content." : content)
                    .setTimestamp(Instant.now())
                    .setColor(COLOUR)
                    .build()
            )
            .setAllowedMentions(ALLOWED_MENTIONS)
            .setComponents(getActionRows());
    }

    public List<ActionRow> getActionRows() {
        final var list = new ArrayList<ActionRow>();
        final var snoozers = TheCommander.getInstance().getGeneralConfig().features().reminders().getSnoozingTimes();
        if (!snoozers.isEmpty()) {
            list.add(ActionRow.of(
                snoozers.stream().map(SnoozingListener.INSTANCE::createSnoozeButton).toList()
            ));
        }
        list.add(ActionRow.of(DismissListener.createDismissButton(ownerId)));
        return list;
    }
}
