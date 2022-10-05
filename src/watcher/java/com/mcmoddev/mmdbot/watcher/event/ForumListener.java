package com.mcmoddev.mmdbot.watcher.event;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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
