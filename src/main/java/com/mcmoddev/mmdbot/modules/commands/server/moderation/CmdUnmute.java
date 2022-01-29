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
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.mcmoddev.mmdbot.MMDBot.LOGGER;
import static com.mcmoddev.mmdbot.MMDBot.getConfig;
import static com.mcmoddev.mmdbot.utilities.console.MMDMarkers.MUTING;

/**
 * Mute the given user, by removing the designated muted role.
 * Takes a user parameter.
 * <p>
 * Takes the form:
 * /unmute ProxyNeko
 *
 * @author ProxyNeko
 * @author Curle
 */
public final class CmdUnmute extends SlashCommand {

    private static final EnumSet<Permission> REQUIRED_PERMISSIONS = EnumSet.of(Permission.MANAGE_ROLES);

    /**
     * Instantiates a new Cmd unmute.
     */
    public CmdUnmute() {
        super();
        name = "unmute";
        help = "Un-mutes a user.";
        category = new Category("Moderation");
        arguments = "<userID/mention";
        requiredRole = "Staff";
        guildOnly = true;
        botPermissions = REQUIRED_PERMISSIONS.toArray(new Permission[0]);

        OptionData data = new OptionData(OptionType.USER, "user", "The user to unmute.").setRequired(true);
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(data);
        this.options = dataList;
    }

    /**
     * Execute.
     *
     * @param event The {@link SlashCommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        final var guild = event.getGuild();
        final var author = guild.getMember(event.getUser());
        if (author == null) {
            return;
        }

        Member user = event.getOption("user").getAsMember();

        final var mutedRoleID = getConfig().getRole("muted");
        final var mutedRole = guild.getRoleById(mutedRoleID);

        if (mutedRole == null) {
            event.reply("An error occurred, please check console for more info...").setEphemeral(true).queue();
            LOGGER.error(MUTING, "Unable to find muted role {}", mutedRoleID);
            return;
        }

        guild.removeRoleFromMember(user, mutedRole).queue();
        event.replyFormat("Un-muted user %s.", user.getAsMention()).setEphemeral(true).queue();
        LOGGER.info(MUTING, "User {} was un-muted by {}", user, author);
    }
}
