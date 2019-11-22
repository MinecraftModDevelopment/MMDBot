package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;

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

        if (MMDBot.getConfig().getGuildID().equals(guildId)) {
            embed.setColor(Color.RED);
            embed.setTitle("User Left");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getName() + " #" + user.getDiscriminator(), true);
            embed.addField("User ID:", user.getId(), true);
            //TODO Get the roles a user has on leaving the server and save them to a dat file of sorts or a DB.
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
