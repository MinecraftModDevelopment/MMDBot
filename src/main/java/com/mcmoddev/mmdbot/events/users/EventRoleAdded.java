package com.mcmoddev.mmdbot.events.users;

import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
public final class EventRoleAdded extends ListenerAdapter {

    /**
     *
     */
    @Override
    public void onGuildMemberRoleAdd(final GuildMemberRoleAddEvent event) {
        final User target = event.getUser();
        final Guild guild = event.getGuild();
        final long channelID = getConfig().getChannel("events.important");

        if (getConfig().getGuildID() != guild.getIdLong())
            return; // Make sure that we don't post if it's not related to 'our' guild

        Utils.getChannelIfPresent(channelID, channel ->
            guild.retrieveAuditLogs()
                .type(ActionType.MEMBER_ROLE_UPDATE)
                .limit(1)
                .cache(false)
                .map(list -> list.get(0))
                .flatMap(entry -> {
                    final List<Role> previousRoles = new ArrayList<>(event.getMember().getRoles());
                    final List<Role> addedRoles = new ArrayList<>(event.getRoles());
                    previousRoles.removeAll(addedRoles); // Just if the member has already been updated

                    final EmbedBuilder embed = new EmbedBuilder();

                    embed.setColor(Color.YELLOW);
                    embed.setTitle("User Role(s) Added");
                    embed.setThumbnail(target.getEffectiveAvatarUrl());
                    embed.addField("User:", target.getAsMention() + " (" + target.getId() + ")", true);
                    if (entry.getTargetIdLong() != target.getIdLong()) {
                        LOGGER.warn(EVENTS, "Inconsistency between target of retrieved audit log entry and actual role event target: retrieved is {}, but target is {}", target, entry.getUser());
                    } else if (entry.getUser() != null) {
                        final User editor = entry.getUser();
                        embed.addField("Editor:", editor.getAsMention() + " (" + editor.getId() + ")", true);
                    }
                    embed.addField("Previous Role(s):", previousRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining(" ")), false);
                    embed.addField("Added Role(s):", addedRoles.stream().map(IMentionable::getAsMention).collect(Collectors.joining(" ")), false);
                    embed.setTimestamp(Instant.now());

                    LOGGER.info(EVENTS, "Role(s) {} was added to user {} by {}", addedRoles, target, entry.getUser());

                    return channel.sendMessage(embed.build());
                })
                .queue()
        );
    }
}
