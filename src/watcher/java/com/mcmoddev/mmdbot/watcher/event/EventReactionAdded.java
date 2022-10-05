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

import club.minnced.discord.webhook.send.AllowedMentions;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.webhook.WebhookManager;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;

/**
 * The type Event reaction added.
 */
public final class EventReactionAdded extends ListenerAdapter {

    private static final String WEBHOOK_NAME = "RequestLogs";
    public static final WebhookManager WEBHOOKS = WebhookManager.of(e -> e.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, AllowedMentions.none());

    /**
     * The Warned messages.
     */
    private final Set<Message> warnedMessages = new HashSet<>();

    /**
     * The set of messages that have passed the reaction threshold required for request deletion, but are awaiting a
     * staff member (a user with {@link Permission#MODERATE_MEMBERS}) to sign-off on the deletion by giving their own
     * reaction.
     *
     * <p>If a message has been added previously to this set, and the message falls back below the request deletion
     * threshold, it will be removed from this set.</p>
     */
    private final Set<Long> messagesAwaitingSignoff = new HashSet<>();

    /**
     * On message reaction add.
     *
     * @param event the event
     */
    @Override
    public void onMessageReactionAdd(final MessageReactionAddEvent event) {
        if (!event.isFromGuild() || !event.isFromType(ChannelType.GUILD_PUBLIC_THREAD)) return;
        final var channel = event.getChannel().asThreadChannel();
        if (TheWatcher.getOldConfig().getChannel("requests.main") != channel.getParentChannel().getIdLong()) return;

        final var message = channel.retrieveMessageById(event.getMessageId())
            .complete();
        if (message == null) {
            return;
        }
        final double removalThreshold = TheWatcher.getOldConfig().getRequestsRemovalThreshold();
        final double warningThreshold = TheWatcher.getOldConfig().getRequestsWarningThreshold();
        if (removalThreshold == 0 || warningThreshold == 0) {
            return;
        }

        final var guild = event.getGuild();
        final int freshnessDuration = TheWatcher.getOldConfig().getRequestFreshnessDuration();
        if (freshnessDuration > 0) {
            final OffsetDateTime creationTime = message.getTimeCreated();
            final var now = OffsetDateTime.now();
            if (now.minusDays(freshnessDuration).isAfter(creationTime)) {
                return; // Do nothing if the request has gone past the freshness duration
            }
        }

        final List<Long> badReactionsList = TheWatcher.getOldConfig().getBadRequestsReactions();
        final List<Long> goodReactionsList = TheWatcher.getOldConfig().getGoodRequestsReactions();
        final List<Long> needsImprovementReactionsList = TheWatcher.getOldConfig().getRequestsNeedsImprovementReactions();
        final var badReactions = getMatchingReactions(message, badReactionsList::contains);

        final List<Member> signedOffStaff = badReactions.stream()
            .map(MessageReaction::retrieveUsers)
            .flatMap(PaginationAction::stream)
            .map(guild::getMember)
            .filter(Objects::nonNull)
            .filter(member -> member.hasPermission(Permission.MODERATE_MEMBERS))
            .toList();
        final var hasStaffSignoff = signedOffStaff.size() > 0;

        final int badReactionsCount = badReactions.stream().mapToInt(MessageReaction::getCount).sum();
        final int goodReactionsCount = getNumberOfMatchingReactions(message, goodReactionsList::contains);
        final int needsImprovementReactionsCount = getNumberOfMatchingReactions(message,
            needsImprovementReactionsList::contains);

        final double requestScore = (badReactionsCount + needsImprovementReactionsCount * 0.5) - goodReactionsCount;

        final User messageAuthor = message.getAuthor();
        if (requestScore >= removalThreshold) {
            // If the message has no staff signing off, skip the rest of the code
            if (!hasStaffSignoff) {

                // If it hasn't been logged about yet, log about it
                if (messagesAwaitingSignoff.add(message.getIdLong())) {
                    final var logChannel = guild.getTextChannelById(TheWatcher.getOldConfig()
                        .getChannel("events.requests_deletion"));
                    if (logChannel != null) {
                        final EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle("Request awaiting moderator approval");
                        builder.appendDescription("Request from ")
                            .appendDescription(messageAuthor.getAsMention())
                            .appendDescription(" has a score of " + requestScore)
                            .appendDescription(", reaching removal threshold of " + removalThreshold)
                            .appendDescription(" and is now awaiting moderator approval before deletion.");
                        builder.addField("Jump to Message",
                            MarkdownUtil.maskedLink("Message in " + message.getChannel().asTextChannel().getAsMention(),
                                message.getJumpUrl()), true);
                        builder.setTimestamp(Instant.now());
                        builder.setColor(Color.YELLOW);
                        builder.setFooter("User ID: " + messageAuthor.getId());

                        WEBHOOKS.getWebhook(logChannel)
                            .send(Utils.webhookMessage(builder.build())
                                .setUsername(messageAuthor.getAsTag())
                                .setAvatarUrl(messageAuthor.getEffectiveAvatarUrl())
                                .build());
                    }
                }
                return;
            }

            final var response = new MessageCreateBuilder().addContent(messageAuthor.getAsMention()).addContent(", ")
                .addContent("your request has been found to be low quality by community review and has been removed.\n")
                .addContent("Please see other requests for how to do it correctly.\n")
                .addContent(String.format("It received %d 'bad' reactions, %d 'needs improvement' reactions, and %d "
                        + "'good' reactions.",
                    badReactionsCount, needsImprovementReactionsCount, goodReactionsCount))
                .build();

            warnedMessages.remove(message);

            final var logChannel = guild.getTextChannelById(TheWatcher.getOldConfig()
                .getChannel("events.requests_deletion"));
            if (logChannel != null) {
                final EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Deleted request by community review");
                builder.appendDescription("Deleted request from ")
                    .appendDescription(messageAuthor.getAsMention())
                    .appendDescription(" which has a score of " + requestScore)
                    .appendDescription(", reaching removal threshold of " + removalThreshold)
                    .appendDescription(", and has been approved by moderators for deletion.");

                final String approvingMods = signedOffStaff.stream()
                    .map(s -> "%s (%s, id `%s`)".formatted(s.getAsMention(), s.getUser().getAsTag(), s.getId()))
                    .collect(Collectors.joining("\n"));
                builder.addField("Approving moderators", approvingMods, true);

                builder.setTimestamp(Instant.now());
                builder.setColor(Color.RED);
                builder.setFooter("User ID: " + messageAuthor.getId());

                WEBHOOKS.getWebhook(logChannel)
                    .send(Utils.webhookMessage(builder.build())
                        .setContent(message.getContentRaw())
                        .setAvatarUrl(messageAuthor.getEffectiveAvatarUrl())
                        .setUsername(messageAuthor.getAsTag())
                        .build());
            }

            final ArrayList<ForumTagSnowflake> tags = new ArrayList<>(channel.getAppliedTags().size());
            channel.getAppliedTags().forEach(it -> tags.add(ForumTagSnowflake.fromId(it.getId())));
            if (tags.size() >= ForumChannel.MAX_POST_TAGS) {
                tags.removeAll(tags.subList(ForumChannel.MAX_POST_TAGS - 4, tags.size()));
            }

            channel.getParentChannel().asForumChannel().getAvailableTags()
                .stream().filter(it -> it.getName().toLowerCase(Locale.ROOT).contains("invalid"))
                .findFirst().map(it -> ForumTagSnowflake.fromId(it.getId()))
                .ifPresent(tag -> {
                    if (tags.isEmpty()) {
                        tags.add(tag);
                    } else {
                        tags.add(0, tag);
                    }
                });

            channel.sendMessage("Request has been removed by community review!")
                .flatMap($msg -> message.delete())
                .flatMap($msg ->
                    channel.getManager()
                        .setName("[INVALID] " + (channel.getName().length() > ThreadChannel.MAX_NAME_LENGTH - 10 ? channel.getName().substring(0, ThreadChannel.MAX_NAME_LENGTH) : channel.getName()))
                        .setLocked(true)
                        .setAppliedTags(tags)
                        .reason(String.format(
                            "Bad request: %d bad reactions, %d needs improvement reactions, %d good reactions",
                            badReactionsCount, needsImprovementReactionsCount, goodReactionsCount))
                        .flatMap(v -> messageAuthor.openPrivateChannel()
                            .flatMap(privateChannel -> privateChannel.sendMessage(response))
                            // If we can't DM the user, send it in the thread.
                            .onErrorFlatMap(throwable -> channel.getManager().setLocked(false)
                                .flatMap(i -> channel.sendMessage(response))
                                .flatMap(i -> channel.getManager().setLocked(true).map($ -> i)))))
                .queue();

        } else if (!warnedMessages.contains(message) && requestScore >= warningThreshold) {

            final var response = new MessageCreateBuilder()
                .addContent(messageAuthor.getAsMention()).addContent(", ")
                .addContent("your request is close to being removed by community review.\n")
                .addContent("Please edit your message to bring it to a higher standard.\n")
                .addContent(String.format("It has so far received %d 'bad' reactions, %d 'needs improvement' reactions, "
                        + "and %d 'good' reactions.",
                    badReactionsCount, needsImprovementReactionsCount, goodReactionsCount))
                .build();

            warnedMessages.add(message);

            RestAction<Message> action = messageAuthor.openPrivateChannel()
                .flatMap(privateChannel -> privateChannel.sendMessage(response))
                // If we can't DM the user, send it in the thread.
                .onErrorFlatMap(throwable -> event.getGuild().getThreadChannelById(event.getMessageIdLong()).sendMessage(response));
            action.queue();
        }
        // Remove messages under the removal threshold from the awaiting sign-off set
        if (requestScore < removalThreshold) {
            messagesAwaitingSignoff.remove(message.getIdLong());
        }
    }

    /**
     * Gets reactions matching a predicate
     *
     * @param message   The message we are getting the matching reactions from.
     * @param predicate The predicate
     * @return The matching reactions.
     */
    public static List<MessageReaction> getMatchingReactions(final Message message, final LongPredicate predicate) {
        List<MessageReaction> reactions = message.getReactions();
        List<MessageReaction> matches = new ArrayList<>();
        for (MessageReaction react : reactions) {
            final var emote = switch (react.getEmoji().getType()) {
                case CUSTOM -> (CustomEmoji) react.getEmoji();
                case UNICODE -> null;
            };
            if (emote != null)
                if (predicate.test(emote.getIdLong()))
                    matches.add(react);
        }
        return matches;
    }

    /**
     * Gets number of matching reactions.
     *
     * @param message   The message we are getting the number of matching reactions from.
     * @param predicate the predicate
     * @return The amount of matching reactions.
     */
    public static int getNumberOfMatchingReactions(final Message message, final LongPredicate predicate) {
        return message
            .getReactions()
            .stream()
            .filter(messageReaction -> messageReaction.getEmoji().getType() == Emoji.Type.CUSTOM)
            .filter(messageReaction -> predicate.test(((CustomEmoji) messageReaction.getEmoji()).getIdLong()))
            .mapToInt(MessageReaction::getCount)
            .sum();
    }
}
