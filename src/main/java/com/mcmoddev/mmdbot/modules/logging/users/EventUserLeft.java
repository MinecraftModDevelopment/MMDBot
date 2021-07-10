package com.mcmoddev.mmdbot.modules.logging.users;

import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.console.MMDMarkers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;

/**
 * The type Event user left.
 *
 * @author
 */
public final class EventUserLeft extends ListenerAdapter {

    /**
     * On guild member remove.
     *
     * @param event the event
     */
    @Override
    public void onGuildMemberRemove(final GuildMemberRemoveEvent event) {
        final var guild = event.getGuild();
        final var channel = guild.getTextChannelById(getConfig().getChannel("events.basic"));
        if (channel == null) {
            return;
        }
        final var member = event.getMember();

        final var guildId = guild.getIdLong();
        if (getConfig().getGuildID() == guildId) {
            final var user = event.getUser();
            LOGGER.info(MMDMarkers.EVENTS, "User {} left the guild", user);
            List<Role> roles = null;
            if (member != null) {
                roles = member.getRoles();
                Utils.writeUserRoles(user.getIdLong(), roles);
            } else {
                LOGGER.warn(MMDMarkers.EVENTS, "Could not get roles of leaving user {}", user);
            }
            if (member != null) {
                Utils.writeUserJoinTimes(user.getId(), member.getTimeJoined().toInstant());
            }

            deleteRecentRequests(guild, user);

            final var embed = new EmbedBuilder();
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            if (roles != null && !roles.isEmpty()) {
                embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention)
                    .collect(Collectors.joining()), true);
                LOGGER.info(MMDMarkers.EVENTS, "User {} had the following roles before leaving: {}", user, roles);
            } else if (roles == null) {
                embed.addField("Roles:", "_Could not obtain user's roles._", true);
            }
            embed.setFooter("User ID: " + user.getId());
            embed.setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }

    /**
     * Delete recent requests.
     *
     * @param guild       the guild
     * @param leavingUser the leaving user
     */
    private void deleteRecentRequests(final Guild guild, final User leavingUser) {
        final var requestsChannel = guild.getTextChannelById(getConfig()
            .getChannel("requests.main"));
        final int deletionTime = getConfig().getRequestLeaveDeletionTime();
        if (requestsChannel != null && deletionTime > 0) {
            final OffsetDateTime now = OffsetDateTime.now().minusHours(deletionTime);
            requestsChannel.getIterableHistory()
                .takeWhileAsync(message -> message.getTimeCreated().isAfter(now)
                    && message.getAuthor().equals(leavingUser))
                .thenAccept(messages ->
                    messages.forEach(message -> {
                        LOGGER.info(MMDMarkers.REQUESTS, "Removed request from {} (current leave deletion of "
                                + "{} hour(s), sent {}) because they left the server",
                            leavingUser, message.getTimeCreated(), deletionTime);

                        final var logChannel = guild.getTextChannelById(getConfig()
                            .getChannel("events.requests_deletion"));
                        if (logChannel != null) {
                            logChannel.sendMessage(String.format("Auto-deleted request from %s (%s;`%s`) "
                                    + "due to leaving server: %n%s", leavingUser.getAsMention(), leavingUser.getAsTag(),
                                leavingUser.getId(), message.getContentRaw()))
                                .allowedMentions(Collections.emptySet())
                                .queue();
                        }

                        message.delete()
                            .reason(String.format("User left, message created at %s, within leave deletion threshold "
                                + "of %s hour(s)", message.getTimeCreated(), deletionTime))
                            .queue();
                    }));
        }
    }
}
