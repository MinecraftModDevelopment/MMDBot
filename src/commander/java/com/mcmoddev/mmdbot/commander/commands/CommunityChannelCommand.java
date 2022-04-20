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
package com.mcmoddev.mmdbot.commander.commands;

import com.google.common.collect.Sets;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.config.GuildConfiguration;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
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
 * @author matyrobbrt
 */
@SuppressWarnings("unused")
public final class CommunityChannelCommand extends SlashCommand {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final EnumSet<Permission> REQUIRED_PERMISSIONS
        = EnumSet.of(Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL);

    @RegisterSlashCommand
    public static final CommunityChannelCommand CMD = new CommunityChannelCommand();

    /**
     * Instantiates a new Cmd community channel.
     */
    public CommunityChannelCommand() {
        super();
        name = "community-channel";
        help = "Creates a new community channel for the given user.";
        category = new Category("Moderation");
        arguments = "<user ID/mention> <channel name>";
        userPermissions = REQUIRED_PERMISSIONS.toArray(Permission[]::new);
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
        final var guild = Objects.requireNonNull(event.getGuild());

        final var user = Objects.requireNonNull(event.getOption("user", OptionMapping::getAsMember));
        final var channel = event.getOption("channel", "", OptionMapping::getAsString);

        final var guildCfg = Objects.requireNonNull(event.getGuildSettings(GuildConfiguration.class))
            .channels().community();

        final var category = guildCfg.category().resolve(guild::getCategoryById);
        if (category == null) {
            event.reply("Community channel category is incorrectly configured. Please contact the bot maintainers.").queue();
            return;
        }

        final var ownerPermissions = guildCfg.ownerPermissions();
        if (ownerPermissions.isEmpty()) {
            TheCommander.LOGGER.warn("Community channel owner permissions is incorrectly configured");
            event.reply("Channel owner permissions is incorrectly configured. Please contact the bot maintainers.").queue();
            return;
        }

        final Set<Permission> diff = Sets.difference(ownerPermissions, guild.getSelfMember().getPermissions());
        if (!diff.isEmpty()) {
            TheCommander.LOGGER.warn("Cannot assign permissions to channel owner due to insufficient permissions: {}", diff);
            event.reply("Cannot assign certain permissions to channel owner due to insufficient permissions; "
                + "continuing anyway...").queue();
            ownerPermissions.removeIf(diff::contains);
        }

        event.deferReply()
            .flatMap(hook -> Objects.requireNonNull(category).createTextChannel(channel)
                .flatMap(ch -> category.modifyTextChannelPositions()
                    .sortOrder(Comparator.comparing(GuildChannel::getName))
                    .map($ -> ch))
                .flatMap(ch -> ch.putPermissionOverride(user).setAllow(ownerPermissions).map($ -> ch))
                .flatMap(ch -> ch.sendMessage(new MessageBuilder()
                        .append(formatMessage(
                            guildCfg.getChannelCreatedMessage(),
                            user,
                            ch
                        ))
                        .build())
                    .flatMap(Message::pin)
                    .map($ -> ch)
                )
                .flatMap(ch -> hook.editOriginal("Successfully created community channel at " + ch.getAsMention() + "!")))
            .queue();
    }

    public static String formatMessage(String msg, Member owner, TextChannel channel) {
        return msg.replace("{user}", owner.getAsMention())
            .replace("{guild}", channel.getGuild().getName())
            .replace("{channel}", channel.getAsMention());
    }
}
