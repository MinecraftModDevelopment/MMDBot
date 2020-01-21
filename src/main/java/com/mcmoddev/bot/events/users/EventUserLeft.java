package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
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
    public void onGuildMemberLeave(final GuildMemberLeaveEvent event) {
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
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getName() + " #" + user.getDiscriminator(), true);
            embed.addField("User ID:", user.getId(), true);
            embed.addField("Roles:", roles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), true);
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
