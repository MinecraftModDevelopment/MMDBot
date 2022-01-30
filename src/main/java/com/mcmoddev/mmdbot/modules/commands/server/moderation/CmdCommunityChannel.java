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

import com.google.common.collect.Sets;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.utilities.Utils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a community channel owned by the specified user.
 * Takes a user parameter and a string parameter.
 * <p>
 * Takes the form:
 * /community-channel KiriCattus Proxy's Trinkets
 * /community-channel SomebodyElse Another Channel With A Very Long Name That Discord Will Reject
 * /community-channel [user] [name]
 *
 * @author Unknown
 * @author Curle
 */
public final class CmdCommunityChannel extends SlashCommand {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final EnumSet<Permission> REQUIRED_PERMISSIONS
        = EnumSet.of(Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL);

    /**
     * Instantiates a new Cmd community channel.
     */
    public CmdCommunityChannel() {
        super();
        name = "community-channel";
        help = "Creates a new community channel for the given user.";
        category = new Category("Moderation");
        arguments = "<user ID/mention> <channel name>";
        requiredRole = "Moderators";
        aliases = new String[]{"community-channel", "comm-ch"};
        guildOnly = true;
        botPermissions = REQUIRED_PERMISSIONS.toArray(new Permission[0]);


        OptionData user = new OptionData(OptionType.USER, "user", "The user to create the channel for.").setRequired(true);
        OptionData channelName = new OptionData(OptionType.STRING, "channel", "The name of the channel to create.").setRequired(true);
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(user);
        dataList.add(channelName);
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
        String channel = event.getOption("channel").getAsString();

        final var categoryID = MMDBot.getConfig().getCommunityChannelCategory();
        final var category = guild.getCategoryById(categoryID);
        if (categoryID == 0 || category == null) {
            MMDBot.LOGGER.warn("Community channel category is incorrectly configured");
            event.reply("Community channel category is incorrectly configured. Please contact the bot maintainers.").queue();
            return;
        }

        final Set<Permission> ownerPermissions = MMDBot.getConfig().getCommunityChannelOwnerPermissions();
        if (ownerPermissions.isEmpty()) {
            MMDBot.LOGGER.warn("Community channel owner permissions is incorrectly configured");
            event.reply("Channel owner permissions is incorrectly configured. Please contact the bot maintainers.").queue();
            return;
        }

        final Set<Permission> diff = Sets.difference(ownerPermissions, event.getGuild().getSelfMember().getPermissions());
        if (!diff.isEmpty()) {
            MMDBot.LOGGER.warn("Cannot assign permissions to channel owner due to insufficient permissions: {}", diff);
            event.reply("Cannot assign certain permissions to channel owner due to insufficient permissions; "
                + "continuing anyway...").queue();
            ownerPermissions.removeIf(diff::contains);
        }

        final List<Emote> emote = new ArrayList<>(4);
        emote.addAll(guild.getEmotesByName("mmd1", true));
        emote.addAll(guild.getEmotesByName("mmd2", true));
        emote.addAll(guild.getEmotesByName("mmd3", true));
        emote.addAll(guild.getEmotesByName("mmd4", true));

        // Flavor text: if the emotes are available, use them, else just use plain MMD
        final var emoteText = emote.size() == 4 ? emote.stream().map(Emote::getAsMention)
            .collect(Collectors.joining()) : "";
        final var flavorText = emoteText.isEmpty() ? "MMD" : emoteText;

        MMDBot.LOGGER.info("Creating new community channel for {} ({}), named \"{}\" (command issued by {} ({}))",
            user.getEffectiveName(), user.getUser().getName(), channel, author.getEffectiveName(), author.getUser().getName());
        category.createTextChannel(channel)
            .flatMap(ch -> category.modifyTextChannelPositions()
                .sortOrder(Comparator.comparing(GuildChannel::getName))
                .map($ -> ch))
            .flatMap(ch -> ch.putPermissionOverride(user).setAllow(ownerPermissions).map($ -> ch))
            .flatMap(ch -> ch.sendMessage(new MessageBuilder()
                    .appendFormat("Welcome %s to your new community channel, %s!%n", user.getAsMention(),
                        ch.getAsMention())
                    .append('\n')
                    .append("Please adhere to the Code of Conduct of the server")
                    .appendFormat(" (which can be visited from the <#%s> channel),", MMDBot.getConfig()
                        .getChannel("info.rules"))
                    .append(" specifically the Channel Policy section.").append('\n')
                    .append('\n')
                    .appendFormat("Thank you, and enjoy your new home here at %s!", flavorText)
                    .build())
                .map($ -> ch)
            )
            .queue(c -> event.reply("Successfully created community channel at " + c.getAsMention() + "!").queue());
    }
}
