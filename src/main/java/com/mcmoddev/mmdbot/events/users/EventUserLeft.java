package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.MMDBot;
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
        final Long guildId = guild.getIdLong();
        final TextChannel channel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDBasicEvents());
        final Member member = event.getMember();
        final List<Role> roles;
        roles = member.getRoles();

        if (MMDBot.getConfig().getGuildID().equals(guildId)) {
            Utils.writeUserRoles(user.getIdLong(), roles);
            Utils.writeUserJoinTimes(user.getId(), member.getTimeJoined().toInstant());
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);
            //TODO Check if this works.
            if (roles != null) {
                embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), true);
            } else {
                embed.addField("Roles:", "Users roles currently unobtainable.", true);
            }
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
