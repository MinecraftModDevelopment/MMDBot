/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.modules.commands.server.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;

public final class CmdRolePanel extends SlashCommand {

    private static final EnumSet<Permission> USER_PERMS = EnumSet.of(Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES);

    public CmdRolePanel() {
        name = "role-panel";
        help = "Does things regarding role panels.";
        userPermissions = USER_PERMS.toArray(new Permission[0]);
        children = new SlashCommand[]{
            new AddRole()
        };
    }

    @Override
    protected void execute(final SlashCommandEvent event) {

    }

    private static final class AddRole extends SlashCommand {

        public AddRole() {
            name = "add-role";
            help = "Adds a role to a panel.";
            options = List.of(
                new OptionData(OptionType.STRING, "message_id", "The id of the message containing the role panel.", true),
                new OptionData(OptionType.ROLE, "role", "The role to add to the panel.", true),
                new OptionData(OptionType.STRING, "emote", "The emote which will be associated with that role.", true)
            );
            guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            if (!Utils.checkCommand(this, event)) {
                return;
            }

            try {
                var channel = event.getChannel();
                var msgIdStr = Utils.getOrEmpty(event.getOption("message_id"));
                if (msgIdStr.contains("-")) {
                    channel = event.getGuild().getTextChannelById(msgIdStr.substring(0, msgIdStr.indexOf('-')));
                    msgIdStr = msgIdStr.substring(msgIdStr.indexOf('-') + 1);
                }
                final var channelId = channel.getIdLong();
                final long messageId = Long.parseLong(msgIdStr);
                final var role = event.getOption("role").getAsRole();
                final var emote = Emoji.fromMarkdown(Utils.getOrEmpty(event.getOption("emote")));
                final var emoteStr = getEmoteAsString(emote);

                channel.addReactionById(messageId, emoteStr).queue($ -> {
                    MMDBot.getConfig().addRolePanel(channelId, messageId, emote.isCustom() ? emote.getId() : emoteStr, role.getIdLong());
                    event.deferReply(true).addEmbeds(new EmbedBuilder().setColor(Color.GREEN).setDescription("Action successful. [Jump to message.](%s)"
                        .formatted(Utils.makeMessageLink(event.getGuild().getIdLong(), channelId, messageId))).build()).mentionRepliedUser(false).queue();
                }, e -> event.deferReply(true).setContent("There was an exception while trying to execute that command: **%s**"
                    .formatted(e.getLocalizedMessage())).mentionRepliedUser(false).queue());
            } catch (Exception e) {
                event.deferReply(true).setContent("There was an exception while trying to execute that command: **%s**"
                    .formatted(e.getLocalizedMessage())).mentionRepliedUser(false).queue();
                MMDBot.LOGGER.error("There was an error running the `/role-panel add-role` command", e);
            }
        }
    }

    public static String getEmoteAsString(final Emoji emoji) {
        return emoji.isCustom() ? emoji.getAsMention().replaceAll("<", "").replaceAll(">", "") : EncodingUtil.encodeCodepoints(emoji.getName());
    }

}
