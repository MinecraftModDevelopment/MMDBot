package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.time.Instant;

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
        final User target = event.getUser();
        final Guild guild = event.getGuild();
        final long channelID = getConfig().getChannel("events.basic");
        final String oldNick = event.getOldNickname() != null ? event.getOldNickname() : target.getName();
        final String newNick = event.getNewNickname() != null ? event.getNewNickname() : target.getName();

        if (getConfig().getGuildID() != guild.getIdLong())
            return; // Make sure that we don't post if it's not related to 'our' guild

        Utils.getChannelIfPresent(channelID, channel ->
            guild.retrieveAuditLogs()
                .type(ActionType.MEMBER_UPDATE)
                .limit(1)
                .cache(false)
                .map(list -> list.get(0))
                .flatMap(entry -> {
                    final EmbedBuilder embed = new EmbedBuilder();

                    embed.setColor(Color.YELLOW);
                    embed.setTitle("Nickname Changed");
                    embed.setThumbnail(target.getEffectiveAvatarUrl());
                    embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")", true);
                    embed.setTimestamp(Instant.now());
                    if (entry.getTargetIdLong() != target.getIdLong()) {
                        LOGGER.warn(EVENTS, "Inconsistency between target of retrieved audit log entry and actual nickname event target: retrieved is {}, but target is {}", target, entry.getUser());
                    } else if (entry.getUser() != null) {
                        final User editor = entry.getUser();
                        embed.addField("Nickname Editor:", editor.getAsMention() + " (" + editor.getId() + ")", true);
                        embed.addBlankField(true);
                    }
                    embed.addField("Old Nickname:", oldNick, true);
                    embed.addField("New Nickname:", newNick, true);

                    LOGGER.info(EVENTS, "User {} changed nickname from `{}` to `{}`, by {}", target, oldNick, newNick, entry.getUser());

                    return channel.sendMessage(embed.build());
                })
                .queue()
        );
    }
}
