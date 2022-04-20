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
import com.mcmoddev.mmdbot.watcher.util.oldchannels.OldChannelsHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Shows a list of channels that haven't been used in a given time period, optionally restricted
 * to specific categories.
 * <p>
 * Optionally takes a days parameter and a category restriction.
 * <p>
 * Takes the form:
 * /old-channels
 * /old-channels 7
 * /old-channels 7 899011928091918388
 * /old-channels [days] [category]
 * <p>
 * // TODO: This seems to be skipping channels for a reason i can't pin down.
 *
 * @author Curle
 */
public final class CmdOldChannels extends SlashCommand {

    /**
     * The constant REQUIRED_PERMISSIONS.
     */
    private static final Permission[] REQUIRED_PERMISSIONS = {
        Permission.MODERATE_MEMBERS
    };

    /**
     * Instantiates a new Cmd old channels.
     */
    public CmdOldChannels() {
        super();
        name = "old-channels";
        help = "Gives channels which haven't been used in an amount of days given as an argument (default 60).";
        category = new Category("Moderation");
        arguments = "[threshold] [channel or category list, separated by spaces]";
        guildOnly = true;
        userPermissions = REQUIRED_PERMISSIONS;

        OptionData days = new OptionData(OptionType.NUMBER, "days", "The minimum amount of days for a channel to show up in the list.").setRequired(false);
        OptionData category = new OptionData(OptionType.CHANNEL, "category", "The category to search for channels in.").setRequired(false);
        List<OptionData> dataList = new ArrayList<>();
        dataList.add(days);
        dataList.add(category);
        this.options = dataList;

    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        final var guild = event.getGuild();
        final var embed = new EmbedBuilder();

        if (!OldChannelsHelper.isReady()) {
            event.reply("Command is still setting up. Please try again in a few moments.").setEphemeral(true).queue();
            return;
        }

        OptionMapping days = event.getOption("days");
        OptionMapping category = event.getOption("category");

        if (category != null && event.getGuild().getCategoryById(category.getAsLong()) == null) {
            event.reply("That category doesn't exist.").setEphemeral(true).queue();
            return;
        }

        final var dayThreshold = days == null ? 60 : days.getAsDouble();

        embed.setTitle("Days since last message in channels:");
        embed.setColor(Color.YELLOW);

        guild.getTextChannels().stream()
            .distinct()
            .filter(c -> category == null || c.getParentCategory() != null && c.getParentCategoryIdLong() == category.getAsLong())
            .map(channel -> new ChannelData(channel, OldChannelsHelper.getLastMessageTime(channel)))
            .forEach(channelData -> {
                if (channelData.days > dayThreshold) {
                    embed.addField("#" + channelData.channel.getName(),
                        String.valueOf(channelData.days), true);
                } else if (channelData.days == -1) {
                    embed.addField("#" + channelData.channel.getName(), "Never had a message", true);
                }
            });

        event.replyEmbeds(embed.build()).queue();
    }

    /**
     * The type Channel data.
     */
    private record ChannelData(TextChannel channel, long days) {

        /**
         * Instantiates a new Channel data.
         *
         * @param channel the channel
         * @param days    the days
         */
        private ChannelData {
        }
    }
}
