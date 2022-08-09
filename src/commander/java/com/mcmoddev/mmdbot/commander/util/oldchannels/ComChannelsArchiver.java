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
package com.mcmoddev.mmdbot.commander.util.oldchannels;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.util.dao.ComChannelsDAO;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.util.MessageUtilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record ComChannelsArchiver(long guildId, JDA jda) implements Runnable {

    public static final ComponentListener COMPONENTS = TheCommander.getComponentListener("com-channels-archiver")
        .onButtonInteraction(ComChannelsArchiver::onInteraction)
        .build();
    public static final EnumSet<Message.MentionType> ALLOWED_MENTIONS = EnumSet.of(
        Message.MentionType.USER
    );

    @Override
    public void run() {
        final var guild = jda.getGuildById(guildId);
        if (guild == null || !OldChannelsHelper.isReady())
            return;
        final var category = TheCommander.getInstance().getConfigForGuild(guildId).channels().community().category().resolve(guild::getCategoryById);
        if (category == null)
            return;
        final var archivalTimestamp = Instant.now().minus(
            TheCommander.getInstance().getConfigForGuild(guildId).channels().community().archivalDuration(),
            ChronoUnit.DAYS
        );
        final var archivalNotifierChannel = TheCommander.getInstance().getConfigForGuild(guildId).channels().community().archivalNotifier().resolve(id -> jda.getChannelById(MessageChannel.class, id));
        if (archivalNotifierChannel == null || !archivalNotifierChannel.canTalk())
            return;
        category.getTextChannels().forEach(channel -> {
            final var lastUsed = Instant.now().minus(
                OldChannelsHelper.getLastMessageTime(channel),
                ChronoUnit.DAYS
            );
            if (lastUsed.isBefore(archivalTimestamp)) {
                final var embed = startArchivalProcess(channel, lastUsed);
                if (embed != null) {
                    archivalNotifierChannel.sendMessage(embed).queue();
                    // And now, to wait for moderators...
                    TheCommander.getInstance().getJdbi().useExtension(ComChannelsDAO.class,
                        db -> db.setIgnoreUntil(channel.getIdLong(), Instant.now().plus(5, ChronoUnit.DAYS)
                            .getEpochSecond()));
                }
            }
        });
    }

    @Nullable
    private Message startArchivalProcess(TextChannel channel, Instant lastMessage) {
        final var owner = TheCommander.getInstance()
            .getJdbi().withExtension(ComChannelsDAO.class, db -> Optional.ofNullable(db.getOwner(channel.getIdLong()))
                .or(() -> {
                    final var permMember = channel.getMemberPermissionOverrides()
                        .stream()
                        .filter(override -> override.getAllowed().contains(Permission.MANAGE_CHANNEL))
                        .flatMap(override -> Optional.ofNullable(override.getMember()).stream())
                        .findFirst()
                        .map(Member::getIdLong);
                    permMember.ifPresent(aLong -> db.insert(channel.getIdLong(), aLong));
                    return permMember;
                }));
        if (owner.isEmpty())
            return null;
        @Nullable final var ignoreUntil = TheCommander.getInstance().getJdbi()
            .withExtension(ComChannelsDAO.class, db -> db.getIgnoreUntil(channel.getIdLong()));
        if (ignoreUntil != null && Instant.now().isBefore(Instant.ofEpochSecond(ignoreUntil)))
            return null;
        // Ok, so now we know that the channel needs to be archived...
        final boolean previouslySaved = !TheCommander.getInstance().getConfigForGuild(channel.getGuild()).channels().community().allowSecondAsk() &&
            TheCommander.getInstance().getJdbi().withExtension(ComChannelsDAO.class, db -> db.wasPreviouslySaved(channel.getIdLong()));
        // ... and we know if it was previously saved from archival...
        // let's send the message to moderators

        final var embed = new EmbedBuilder()
            .setTitle("Community Channel Archival")
            .setAuthor(jda.getSelfUser().getName(), null, jda.getSelfUser().getAvatarUrl())
            .setDescription("""
                The Community Channel %s hasn't had any activity since %s"""
                .formatted(
                    channel.getAsMention(), TimeFormat.DATE_SHORT.format(lastMessage)
                ))
            .setTimestamp(Instant.now())
            .setColor(Color.ORANGE);

        if (previouslySaved)
            embed.appendDescription(" and was previously saved from archival by its owner, or the owner never responded to an archival request.");
        embed.appendDescription(".\n")
            .appendDescription("You have 5 days to decide if ");

        if (previouslySaved)
            embed.appendDescription("the channel should now be permanently archived");
        else
            embed.appendDescription("I should ask the owner about the archival, or if the channel should be archived");
        embed.appendDescription(", otherwise a new reminder will be sent.");

        final var messageBuilder = new MessageBuilder()
            .setEmbeds(embed.build());

        final List<Button> buttons = new ArrayList<>();

        buttons.add(COMPONENTS.createButton(ButtonStyle.DANGER, "Archive channel", null, Component.Lifespan.PERMANENT, List.of(
            Action.ARCHIVE.toString(),
            owner.get().toString(),
            channel.getId()
        )));
        if (!previouslySaved) {
            buttons.add(COMPONENTS.createButton(ButtonStyle.PRIMARY, "Ask owner", null, Component.Lifespan.PERMANENT, List.of(
                Action.ASK_OWNER.toString(),
                owner.get().toString(),
                channel.getId()
            )));
        }
        buttons.add(COMPONENTS.createButton(ButtonStyle.SUCCESS, "Cancel", null, Component.Lifespan.PERMANENT, List.of(
            Action.CANCEL_ARCHIVAL.toString(),
            owner.get().toString(),
            channel.getId()
        )));
        messageBuilder.setActionRows(ActionRow.of(buttons));

        return messageBuilder.build();
    }

    private static void onInteraction(ButtonInteractionContext context) {
        final var action = Action.byName(context.getArguments().get(0));
        if (context.getGuild() == null)
            return;
        switch (action) {
            case ARCHIVE -> {
                if (context.getUser().getId().equals(context.getArguments().get(1)) || context.getMember().hasPermission(Permission.MANAGE_CHANNEL))
                    doArchive(context, context.getArgument(2, () -> 0L, Long::parseLong));
            }
            case ASK_OWNER -> {
                if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL))
                    askOwner(context, context.getArgument(2, () -> 0L, Long::parseLong));
            }
            case CANCEL_ARCHIVAL -> cancelArchival(context, context.getEvent().getChannel().getIdLong());
        }
    }

    private static void askOwner(ButtonInteractionContext context, long channelId) {
        final var channel = context.getEvent().getJDA().getTextChannelById(channelId);
        final var owner = context.getArgument(1, () -> 0L, Long::parseLong);
        if (channel != null) {
            final var embed = new EmbedBuilder()
                .setTitle("Channel archival")
                .setAuthor(context.getEvent().getJDA().getSelfUser().getName(), null, context.getEvent().getJDA().getSelfUser().getAvatarUrl())
                .setTimestamp(Instant.now())
                .setFooter("MMD Staff")
                .setDescription("""
                    Hello there,
                    We have noticed that your channel has not received activity since %s, and we would like to know if you still want to keep your channel.
                    If so, click the "Cancel" button, otherwise use the "Archive" button to have your channel archived until the end of the year, when it will be deleted.
                    You have 14 days to think, no rush!"""
                    .formatted(
                        TimeFormat.DATE_SHORT.format(
                            Instant.now().minus(OldChannelsHelper.getLastMessageTime(channel), ChronoUnit.DAYS)
                        )
                    ))
                .setColor(Color.CYAN);

            channel.sendMessage(new MessageBuilder()
                .append("<@")
                .append(owner)
                .append(">")
                .setEmbeds(embed.build())
                .setActionRows(ActionRow.of(
                    COMPONENTS.createButton(ButtonStyle.PRIMARY, "Cancel archival", null, Component.Lifespan.PERMANENT, List.of(
                        Action.CANCEL_ARCHIVAL.toString(),
                        owner.toString(),
                        channel.getId()
                    )),
                    COMPONENTS.createButton(ButtonStyle.DANGER, "Archive channel", null, Component.Lifespan.PERMANENT, List.of(
                        Action.ARCHIVE.toString(),
                        owner.toString(),
                        channel.getId()
                    ))
                ))
                .setAllowedMentions(ALLOWED_MENTIONS)
                .build()).queue();

            context.getEvent().reply("Asked <@" + owner + "> if they want their channel archived!")
                .setEphemeral(true)
                .queue();
            TheCommander.getInstance().getJdbi().useExtension(ComChannelsDAO.class,
                db -> db.setIgnoreUntil(channelId, Instant.now().plus(14, ChronoUnit.DAYS).getEpochSecond()));
            MessageUtilities.disableButtons(context.getEvent().getMessage());
            context.getEvent().getMessage()
                .editMessage("Resolved by " + context.getUser().getAsMention())
                .queue();
            context.deleteComponent();
        }
    }

    private static void doArchive(ButtonInteractionContext context, long channelId) {
        context.getEvent().deferEdit().queue();
        final var channel = context.getEvent().getJDA().getTextChannelById(channelId);
        final var archivedCategory = TheCommander.getInstance().getConfigForGuild(context.getGuild()).channels()
            .community().archivedCategory().resolve(context.getEvent().getJDA()::getCategoryById);
        if (archivedCategory != null && channel != null) {
            channel.getManager().setParent(archivedCategory)
                .flatMap($ -> channel.getPermissionContainer().getManager().putMemberPermissionOverride(
                    TheCommander.getInstance().getJdbi().withExtension(ComChannelsDAO.class, db -> db.getOwner(channelId)),
                    Permission.getRaw(Permission.VIEW_CHANNEL),
                    0
                )) // Give the owner view perms of their channel
                .flatMap($ -> channel.sendMessage("This channel has been archived at the request of " + context.getUser().getAsMention() + ", due to lack of activity."))
                .queue();
            TheCommander.getInstance().getJdbi().useExtension(ComChannelsDAO.class,
                db -> db.removeExtraData(channelId));
            MessageUtilities.disableButtons(context.getEvent().getMessage());
            context.getEvent().getMessage()
                .editMessage("Resolved by " + context.getUser().getAsMention())
                .queue();
            context.deleteComponent();
        }
    }

    private static void cancelArchival(ButtonInteractionContext context, long channelId) {
        final var hasPerms = context.getMember().hasPermission(Permission.MANAGE_CHANNEL);
        if (hasPerms || context.getUser().getId().equals(context.getArguments().get(1))) {
            TheCommander.getInstance().getJdbi().useExtension(ComChannelsDAO.class,
                db -> {
                    db.setPreviouslySaved(channelId, !hasPerms);
                    db.setIgnoreUntil(channelId, Instant.now()
                        .plus(TheCommander.getInstance().getConfigForGuild(context.getGuild()).channels()
                            .community().archivalDuration(), ChronoUnit.DAYS)
                        .getEpochSecond());
                });
            context.getEvent().reply("Archival cancelled!")
                .setEphemeral(true)
                .queue();
            context.getEvent().getMessage()
                .editMessage("Resolved by " + context.getUser().getAsMention())
                .queue();
            MessageUtilities.disableButtons(context.getEvent().getMessage());
            context.deleteComponent();
        }
    }

    enum Action {
        CANCEL_ARCHIVAL,
        ASK_OWNER,
        ARCHIVE;

        public static Action byName(String name) {
            for (final var val : values())
                if (name.equalsIgnoreCase(val.toString()))
                    return val;
            throw new UnsupportedOperationException("Unknown value: " + name);
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT);
        }
    }

}
