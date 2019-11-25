package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;

/**
 *
 */
public final class EventNicknameChanged extends ListenerAdapter {

	/**
	 *
	 */
    @Override
    public void onGuildMemberUpdateNickname(final GuildMemberUpdateNicknameEvent event) {
        final User user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Guild guild = event.getGuild();
        final TextChannel channel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDBasicEvents());
        final Long guildId = guild.getIdLong();
        String oldNick;
        String newNick;

        Utils.sleepTimer();

        if (event.getOldNickname() == null) {
            oldNick = user.getName();
        } else {
            oldNick = event.getOldNickname();
        }

        if (event.getNewNickname() == null) {
            newNick = user.getName();
        } else {
            newNick = event.getNewNickname();
        }

        if (MMDBot.getConfig().getGuildID().equals(guildId)) {

            embed.setColor(Color.YELLOW);
            embed.setTitle("Nickname Changed");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getName() + " #" + user.getDiscriminator(), true);
            embed.addField("User ID:", user.getId(), true);
            embed.addField("Old Nickname:", oldNick, true);
            embed.addField("New Nickname:", newNick, true);
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
