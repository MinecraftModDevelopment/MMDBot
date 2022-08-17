/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.watcher.commands.moderation;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Mute the given user, by removing the designated muted role.
 * Takes a user parameter.
 * <p>
 * Takes the form:
 * /unmute KiriCattus
 *
 * TODO log the unmute
 *
 * @author KiriCattus
 * @author Curle
 */
public final class UnmuteCommand extends SlashCommand {

    private static final Permission[] REQUIRED_PERMISSIONS = {
        Permission.MODERATE_MEMBERS
    };

    /**
     * Instantiates a new Cmd unmute.
     */
    public UnmuteCommand() {
        super();
        name = "unmute";
        help = "Un-mutes a user.";
        category = new Category("Moderation");
        arguments = "<userID/mention";
        guildOnly = true;
        botPermissions = REQUIRED_PERMISSIONS;
        userPermissions = REQUIRED_PERMISSIONS;

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

        final var guild = event.getGuild();
        final var author = guild.getMember(event.getUser());
        if (author == null) {
            return;
        }

        Member user = event.getOption("user").getAsMember();

        if (user == null) {
            event.reply(String.format("User %s not found.", event.getOption("user").getAsUser().getName())).setEphemeral(true).queue();
            return;
        }

        guild.removeTimeout(user).reason("Timeout removed at the request of " + event.getUser().getAsTag()).queue();

        final var muteEmbed = new EmbedBuilder().setColor(Color.DARK_GRAY)
            .setTitle(user.getEffectiveName() + " has been un-muted!").setTimestamp(Instant.now())
            .addField("Un-muted User", "%s (%s)".formatted(user.getAsMention(), user.getIdLong()), false)
            .setFooter("Moderator ID: " + author.getIdLong(), author.getEffectiveAvatarUrl());

        user.getUser().openPrivateChannel().queue(dm -> {
            final var embed = new EmbedBuilder().setColor(Color.RED).setTitle("You have been unmuted!")
                .setDescription("You have been un-muted in **" + guild.getName() + "**").setTimestamp(Instant.now())
                .setFooter("Moderator ID: " + author.getIdLong(), author.getEffectiveAvatarUrl());
            dm.sendMessageEmbeds(embed.build()).queue();
        });

        event.replyFormat("Un-muted user %s.", user.getAsMention()).setEphemeral(true).queue();
    }
}
