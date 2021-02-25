package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.EVENTS;

/**
 *
 */
public final class EventUserLeft extends ListenerAdapter {

    /**
     *
     */
    @Override
    public void onGuildMemberRemove(final GuildMemberRemoveEvent event) {
        final User user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final TextChannel channel = guild.getTextChannelById(getConfig().getChannel("events.basic"));
        if (channel == null) return;
        final Member member = event.getMember();

        if (getConfig().getGuildID() == guildId) {
            LOGGER.info(EVENTS, "User {} left the guild", user);
            List<Role> roles = null;
            if (member != null) {
                roles = member.getRoles();
                Utils.writeUserRoles(user.getIdLong(), roles);
            } else {
                LOGGER.warn(EVENTS, "Could not get roles of leaving user {}", user);
            }
            if (member != null) {
                Utils.writeUserJoinTimes(user.getId(), member.getTimeJoined().toInstant());
            }
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            //TODO Check if this works.
            if (roles != null && !roles.isEmpty()) {
                embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), true);
                LOGGER.info(EVENTS, "User {} had the following roles before leaving: {}", user, roles);
            } else if (roles == null) {
                embed.addField("Roles:", "_Could not obtain user's roles._", true);
            }
            embed.setFooter("User ID: " + user.getId());
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
