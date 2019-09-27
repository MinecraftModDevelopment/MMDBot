package com.mcmoddev.bot.events.users;

import com.mcmoddev.bot.misc.BotConfig;
import com.mcmoddev.bot.misc.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EventRoleAdded extends ListenerAdapter {

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        Long guildId = event.getGuild().getIdLong();
        TextChannel channel = event.getGuild().getTextChannelById(BotConfig.getConfig().getChannelIDImportantEvents());

        Utils.sleepTimer();

        //TODO Somehow get the info from the audit log about who edited the role so we can ger the name and ID.
        //AuditLogEntry entry = event.getGuild().getAuditLogs(ActionType.MEMBER_ROLE_UPDATE).getEntriesByTarget(event.getUser().getIdLong()).stream().sorted(Comparator.comparing(AuditLogEntry::getIdLong).reversed()).findFirst().orElse(null);
        //String rolesEditedBy = entry.getResponsibleUser().getName() + "#" + entry.getResponsibleUser().getDiscriminator();
        //String editorID = entry.getResponsibleUser().getStringID();

        List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
        List<Role> addedRoles = new ArrayList<>(event.getRoles());

        if (BotConfig.getConfig().getGuildID().equals(guildId)) {
            embed.setColor(Color.YELLOW);
            embed.setTitle("User Role Added");
            embed.setThumbnail(event.getUser().getEffectiveAvatarUrl());
            embed.addField("User:", event.getUser().getAsTag(), true);
            embed.addField("User ID:", event.getUser().getId(), true);

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