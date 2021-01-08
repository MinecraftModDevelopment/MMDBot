package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.pagination.AuditLogPaginationAction;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.logging.MMDMarkers.EVENTS;

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
        final TextChannel channel = guild.getTextChannelById(getConfig().getChannel("events.basic"));
        if (channel == null) return;
        final long guildId = guild.getIdLong();
        String oldNick;
        String newNick;

        Utils.sleepTimer();

        final AuditLogPaginationAction paginationAction = event.getGuild().retrieveAuditLogs()
                .type(ActionType.MEMBER_UPDATE)
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

        if (getConfig().getGuildID() == guildId) {
            LOGGER.info(EVENTS, "User {} changed nickname from {} to {}, changed by {}", user.getId(), oldNick, newNick, editorID);

            embed.setColor(Color.YELLOW);
            embed.setTitle("Nickname Changed");
            embed.setThumbnail(user.getEffectiveAvatarUrl());
            embed.addField("User:", user.getAsTag(), true);
            embed.addField("User ID:", user.getId(), true);
            embed.addField("Old Nickname:", oldNick, true);
            embed.addField("New Nickname:", newNick, true);
            embed.addField("Nickname Editor:", editorTag, true);
            embed.addField("Nickname Editor ID:", editorID, true);
            embed.setTimestamp(Instant.now());

            channel.sendMessage(embed.build()).queue();
        }
    }
}
