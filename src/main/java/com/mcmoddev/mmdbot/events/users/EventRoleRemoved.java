package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.EVENTS;

/**
 *
 */
public final class EventRoleRemoved extends ListenerAdapter {

    /**
     *
     */
    @Override
    public void onGuildMemberRoleRemove(final GuildMemberRoleRemoveEvent event) {
        final User user = event.getUser();
        final EmbedBuilder embed = new EmbedBuilder();
        final Guild guild = event.getGuild();
        final long guildId = guild.getIdLong();
        final TextChannel channel = guild.getTextChannelById(getConfig().getChannel("events.important"));
        if (channel == null) return;

        Utils.sleepTimer();

        final AuditLogPaginationAction paginationAction = event.getGuild().retrieveAuditLogs()
            .type(ActionType.MEMBER_ROLE_UPDATE)
            .limit(1)
            .cache(false);

        final List<AuditLogEntry> entries = paginationAction.complete();

        final AuditLogEntry entry = entries.get(0);
        final User editor = entry.getUser();

        String editorID = "Unknown";
        String editorTag = "Unknown";
        if (editor != null) {
            editorID = editor.getId();
            editorTag = editor.getAsTag();
        }

        final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
        final List<Role> removedRoles = new ArrayList<>(event.getRoles());
        previousRoles.removeAll(removedRoles);

        if (getConfig().getGuildID() == guildId) {
            LOGGER.info(EVENTS, "Role {} was removed from user {} by {}", removedRoles, user, editor);

            embed.setColor(Color.YELLOW);
            embed.setTitle("User Role Removed");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);
            embed.addField("Edited By:", editorTag, true);
            embed.addField("Editor ID:", editorID, true);
            embed.addField("Previous Roles:", previousRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), false);
            embed.addField("Removed Roles:", removedRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining()), false);
            embed.setTimestamp(Instant.now());
            channel.sendMessage(embed.build()).queue();
        }
    }
}
