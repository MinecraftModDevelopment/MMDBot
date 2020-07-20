package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public final class EventReactionAdded extends ListenerAdapter {

	private Set<Message> warnedMessages = new HashSet<>();

	/**
	 *
	 */
	@Override
	public void onMessageReactionAdd(final MessageReactionAddEvent event) {
		final Guild guild = event.getGuild();
		final Long guildId = guild.getIdLong();
		final TextChannel channel = event.getTextChannel();
		final TextChannel discussionChannel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDRequestsDiscussion());

		MessageHistory history = MessageHistory.getHistoryAround(channel, event.getMessageId()).limit(1).complete();
		final Message message = history.getMessageById(event.getMessageId());
		if (message == null) return;
		final User messageAuthor = message.getAuthor();

		if (MMDBot.getConfig().getGuildID().equals(guildId) && MMDBot.getConfig().getChannelIDRequests().equals(channel.getIdLong())) {
			final int badReactions = Utils.getNumberOfMatchingReactions(message, Utils::isReactionBad);
			final int goodReactions = Utils.getNumberOfMatchingReactions(message, Utils::isReactionGood);
			final int needsImprovementReactions = Utils.getNumberOfMatchingReactions(message, Utils::isReactionNeedsImprovement);

			if ((badReactions + needsImprovementReactions * 0.5) - goodReactions >= MMDBot.getConfig().getBadReactionThreshold()) {
				final TextChannel logChannel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDDeletedMessages());
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

				if (discussionChannel == null) return;
				discussionChannel.sendMessage(responseBuilder.build()).queue();
			} else if (!warnedMessages.contains(message) && (badReactions + needsImprovementReactions * 0.5) - goodReactions >= MMDBot.getConfig().getWarningBadReactionThreshold()) {
				final MessageBuilder responseBuilder = new MessageBuilder();
				responseBuilder.append(messageAuthor.getAsMention());
				responseBuilder.append(", ");
				responseBuilder.append("your request is close to being removed by community review.\n" +
					"Please edit your message to bring it to a higher standard.\n");
				responseBuilder.appendFormat("It has so far received %d 'bad' reactions, %d 'needs improvement' reactions, and %d 'good' reactions.",
					badReactions, needsImprovementReactions, goodReactions);

				warnedMessages.add(message);

				if (discussionChannel == null) return;
				discussionChannel.sendMessage(responseBuilder.build()).queue();
			}
		}
	}
}
