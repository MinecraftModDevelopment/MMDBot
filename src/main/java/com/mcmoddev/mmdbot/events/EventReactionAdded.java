package com.mcmoddev.mmdbot.events;

import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public final class EventReactionAdded extends ListenerAdapter {

    private final Set<Message> warnedMessages = new HashSet<>();

    /**
     *
     */
    @Override
    public void onMessageReactionAdd(final MessageReactionAddEvent event) {
        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final TextChannel channel = event.getTextChannel();
        final TextChannel discussionChannel = guild.getTextChannelById(MMDBot.getConfig().getChannel("requests.discussion"));

        MessageHistory history = MessageHistory.getHistoryAround(channel, event.getMessageId()).limit(1).complete();
        final Message message = history.getMessageById(event.getMessageId());
        if (message == null) return;
        final User messageAuthor = message.getAuthor();
		final double removalThreshold = MMDBot.getConfig().getRequestsRemovalThreshold();
		final double warningThreshold = MMDBot.getConfig().getRequestsWarningThreshold();
		if (removalThreshold == 0 || warningThreshold == 0) return;

        if (MMDBot.getConfig().getGuildID() == guildId && MMDBot.getConfig().getChannel("requests.main") == channel.getIdLong()) {

        	final List<Long> badReactionsList = MMDBot.getConfig().getBadRequestsReactions();
			final List<Long> goodReactionsList = MMDBot.getConfig().getGoodRequestsReactions();
			final List<Long> needsImprovementReactionsList = MMDBot.getConfig().getRequestsNeedsImprovementReactions();
            final int badReactions = Utils.getNumberOfMatchingReactions(message, badReactionsList::contains);
            final int goodReactions = Utils.getNumberOfMatchingReactions(message, goodReactionsList::contains);
            final int needsImprovementReactions = Utils.getNumberOfMatchingReactions(message, needsImprovementReactionsList::contains);

            final double requestScore = (badReactions + needsImprovementReactions * 0.5) - goodReactions;

            if (requestScore >= removalThreshold) {
                final TextChannel logChannel = guild.getTextChannelById(MMDBot.getConfig().getChannel("events.requests_deletion"));
                if (logChannel != null) {
                    logChannel.sendMessage(String.format("Auto-deleted request from %s: %s", messageAuthor.getId(), message.getContentRaw())).queue();
                }
                channel.deleteMessageById(event.getMessageId()).reason(String.format(
                        "Bad request: %d bad reactions, %d needs improvement reactions, %d good reactions",
                        badReactions, needsImprovementReactions, goodReactions)
                ).complete();

                final MessageBuilder responseBuilder = new MessageBuilder();
                responseBuilder.append(messageAuthor.getAsMention());
                responseBuilder.append(", ");
                responseBuilder.append("your request has been found to be low quality by community review and has been removed.\n" +
                        "Please see other requests for how to do it correctly.\n");
                responseBuilder.appendFormat("It received %d 'bad' reactions, %d 'needs improvement' reactions, and %d 'good' reactions.",
                        badReactions, needsImprovementReactions, goodReactions);

                warnedMessages.remove(message);

                Message response = responseBuilder.build();
                messageAuthor.openPrivateChannel().submit().thenCompose(c -> c.sendMessage(response).submit()).whenComplete((msg, throwable) -> {
                    if (throwable != null && discussionChannel != null) discussionChannel.sendMessage(response).queue();
                });
            } else if (!warnedMessages.contains(message) && requestScore >= warningThreshold) {
                final MessageBuilder responseBuilder = new MessageBuilder();
                responseBuilder.append(messageAuthor.getAsMention());
                responseBuilder.append(", ");
                responseBuilder.append("your request is close to being removed by community review.\n" +
                        "Please edit your message to bring it to a higher standard.\n");
                responseBuilder.appendFormat("It has so far received %d 'bad' reactions, %d 'needs improvement' reactions, and %d 'good' reactions.",
                        badReactions, needsImprovementReactions, goodReactions);

                warnedMessages.add(message);

                Message response = responseBuilder.build();
                messageAuthor.openPrivateChannel().submit().thenCompose(c -> c.sendMessage(response).submit()).whenComplete((msg, throwable) -> {
                    if (throwable != null && discussionChannel != null) {
                        discussionChannel.sendMessage(response).queue();
                    }
                });
            }
        }
    }
}
