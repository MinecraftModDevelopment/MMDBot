package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public final class EventRoleAdded extends ListenerAdapter {

	/**
	 *
	 */
    @Override
    public void onGuildMemberRoleAdd(final GuildMemberRoleAddEvent event) {
    	final User user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Guild guild = event.getGuild();
        final Long guildId = guild.getIdLong();
        final TextChannel channel = guild.getTextChannelById(MMDBot.getConfig().getChannelIDImportantEvents());

        Utils.sleepTimer();

        //TODO Somehow get the info from the audit log about who edited the role so we can get the name and ID.
        //AuditLogEntry entry = event.getGuild().getAuditLogs(ActionType.MEMBER_ROLE_UPDATE).getEntriesByTarget(user.getIdLong()).stream().sorted(Comparator.comparing(AuditLogEntry::getIdLong).reversed()).findFirst().orElse(null);
        //String rolesEditedBy = entry.getResponsibleUser().getName() + "#" + entry.getResponsibleUser().getDiscriminator();
        //String editorID = entry.getResponsibleUser().getStringID();

        // TODO Reenable when done.
        //final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
        final List<Role> addedRoles = new ArrayList<>(event.getRoles());

        if (MMDBot.getConfig().getGuildID().equals(guildId)) {
            embed.setColor(Color.YELLOW);
            embed.setTitle("User Role Added");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);

            embed.addField("Edited By:", "Not Yet Provided/Setup", true);
            embed.addField("Editor ID:", "Not Yet Provided/Setup", true);
            //TODO Fix this, it only shows one role from both the previous roles and the added roles, something to do with have to iterate through a list and getAsMention() them.
            //embed.addField("Previous Roles:", previousRoles.get(0).getAsMention(), false);
            embed.addField("Previous Roles:", "Not Yet Provided/Setup in new api.", false);

            embed.addField("Added Roles:", addedRoles.get(0).getAsMention(), false);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        }
    }
}
