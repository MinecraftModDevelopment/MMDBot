package com.mcmoddev.bot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.MMDBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;

/**
 *
 */
public final class CmdRoles extends Command {

	/**
	 * Get the amount of members per role.
	 *
	 * @param guild The guild we are in.
	 * @param id    The ID of the role.
	 * @return
	 */
	private String getRoleMemberCount(final Guild guild, final String id) {
		return Integer.toString(guild.getMembersWithRoles(guild.getRoleById(id)).size());
	}

	/**
	 *
	 */
	public CmdRoles() {
		super();
		name = "roles";
		aliases = new String[]{"roleinfo"};
		help = "Gives a count of users per role. **Locked to <#" + MMDBot.getConfig().getBotStuffChannelId() + ">**";
	}

	/**
	 *
	 */
	@Override
	protected void execute(final CommandEvent event) {
		final Guild guild = event.getGuild();
		final EmbedBuilder embed = new EmbedBuilder();
		final TextChannel channel = event.getTextChannel();

		//TODO Add some more info to this. -Proxy
		//Get the amount of people who have the role specified.
		final String staff = getRoleMemberCount(guild, MMDBot.getConfig().getRoleStaff());
		final String partner = getRoleMemberCount(guild, MMDBot.getConfig().getRolePartner());
		final String communityRep = getRoleMemberCount(guild, MMDBot.getConfig().getRoleCommunityRep());
		final String modder = getRoleMemberCount(guild, MMDBot.getConfig().getRoleModder());
		final String artist = getRoleMemberCount(guild, MMDBot.getConfig().getRoleArtist());
		final String streamer = getRoleMemberCount(guild, MMDBot.getConfig().getRoleStreamer());
		final String modpackMaker = getRoleMemberCount(guild, MMDBot.getConfig().getRoleModpackMaker());
		final String translator = getRoleMemberCount(guild, MMDBot.getConfig().getRoleTranslator());
		final String booster = getRoleMemberCount(guild, MMDBot.getConfig().getRoleBooster());

		embed.setColor(Color.GREEN);
		embed.setTitle("Users With Role");
		embed.setDescription("A count of how many members have been assigned some of MMD's many roles.");

		embed.addField("Staff count:", staff, true);
		embed.addField("Partner count:", partner, true);
		embed.addField("Community Rep count:", communityRep, true);
		embed.addField("Modder count:", modder, true);
		embed.addField("Artist count:", artist, true);
		embed.addField("Streamer count:", streamer, true);
		embed.addField("Modpack Maker count:", modpackMaker, true);
		embed.addField("Translator count:", translator, true);
		embed.addField("Nitro Booster count:", booster, true);
		embed.setTimestamp(Instant.now());

		final long channelID = MMDBot.getConfig().getBotStuffChannelId();
		if (channel.getIdLong() != channelID) {
			channel.sendMessage("This command is channel locked to <#" + channelID + ">").queue();
		} else {
			channel.sendMessage(embed.build()).queue();
		}
	}
}
