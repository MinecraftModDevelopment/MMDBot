/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.watcher.event;

import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ForumListener extends ListenerAdapter {
    @Override
    public void onChannelUpdateAppliedTags(@NotNull final ChannelUpdateAppliedTagsEvent event) {
        final ThreadChannel thread = event.getChannel().asThreadChannel();
        if (thread.isArchived() || thread.isLocked()) return;

        final StringBuilder message = new StringBuilder()
            .append("This post has had tags modified.");

        if (!event.getAddedTags().isEmpty()) {
            message.append("\nAdded tags: ")
                .append(formatTags(event.getAddedTags(), event.getJDA()));
        }

        if (!event.getRemovedTags().isEmpty()) {
            message.append("\nRemoved tags: ")
                .append(formatTags(event.getRemovedTags(), event.getJDA()));
        }

        thread.sendMessage(message).queue();
    }

    public static void onCollectTasks(TaskScheduler.CollectTasksEvent event) {
        event.addTask(() -> {
            if (TheWatcher.getInstance() == null) return;
            final Instant _3DaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
            TheWatcher.getInstance().getJda().getForumChannelCache()
                .stream().flatMap(it -> it.getThreadChannels().stream()).forEach(channel -> {
                final Consumer<Message> lastMessage = (Message last) -> {
                    if (last.getTimeCreated().toInstant().isBefore(_3DaysAgo)) {
                        channel.getManager().setArchived(true).reason("3 days without activity").queue();
                    }
                };
                final var his = channel.getHistory().getRetrievedHistory();
                if (his.isEmpty()) {
                    channel.getHistory().retrievePast(1).queue(it -> {
                        if (!it.isEmpty()) {
                            lastMessage.accept(it.get(0));
                        }
                    });
                } else {
                    lastMessage.accept(his.get(his.size() - 1));
                }
            });
        }, 1, 24, TimeUnit.HOURS);
    }

    private String formatTags(List<ForumTag> tags, JDA jda) {
        return String.join(", ", tags.stream()
            .map(it -> (it.getEmoji() == null ? "" : format(it.getEmoji(), jda) + " ") + "`" + it.getName() + "`")
            .toList());
    }

    private String format(EmojiUnion union, JDA jda) {
        if (union.getType() == Emoji.Type.UNICODE) return union.getFormatted();
        final CustomEmoji customEmoji = union.asCustom();
        return Objects.requireNonNull(jda.getEmojiById(customEmoji.getId())).getFormatted();
    }
}
