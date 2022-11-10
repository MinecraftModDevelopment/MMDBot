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
package com.mcmoddev.mmdbot.commander.custompings;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.reminders.Reminder;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListeners;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener;
import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.mcmoddev.mmdbot.commander.TheCommander.getInstance;

public class CustomPingsListener extends ListenerAdapter {
    public static final LazySupplier<EventListener> LISTENER = LazySupplier.of(() -> new ThreadedEventListener(Executors.newSingleThreadExecutor(r -> Utils.setThreadDaemon(new Thread(r, "CustomPingsListener"), true)),
        new CustomPingsListener()));


    public static final ComponentListener COMPONENTS = TheCommander.getComponentListener("custom-pings")
        .onButtonInteraction(ComponentListeners.checkUser(ComponentListeners.multipleTypes(Map.of(
            "list-removed", CustomPingsListener::onListRemovedPings,
            "restore-removed", CustomPingsListener::onRestoreRemovedPings
        ))))
        .build();

    private CustomPingsListener() {
    }

    @Override
    public void onMessageReceived(@NotNull final MessageReceivedEvent event) {
        if (event.isFromGuild() && event.getChannelType().isMessage() && getInstance().getConfigForGuild(event.getGuild()).features().customPings().areEnabled()
            && event.getAuthor().getIdLong() != event.getJDA().getSelfUser().getIdLong()) {
            handlePings(event.getGuild(), event.getMessage());
        }
    }

    public static void handlePings(final @NonNull Guild guild, @NonNull final Message message) {
        final var author = message.getAuthor();
        final var allPings = CustomPings.getAllPingsInGuild(guild.getIdLong());
        allPings.forEach((userId, pings) -> {
            if (pings.isEmpty()) return;
            if (userId == author.getIdLong()) return;
            guild.retrieveMemberById(userId).queue(user -> {
                // They can't view the channel
                if (!canViewChannel(user, message.getGuildChannel())) return;
                user.getUser().openPrivateChannel().queue(privateChannel -> {
                    final var dmAction = pings.stream()
                        .filter(p -> p.test(message))
                        .findFirst()
                        .map(p -> sendPingMessage(p, message, privateChannel));
                    dmAction.ifPresent(messageAction -> messageAction.queue(null, new ErrorHandler()
                        .handle(ErrorResponse.CANNOT_SEND_TO_USER, e -> {
                            // Can't DM, so clear pings
                            TheCommander.LOGGER.warn("Removing custom pings for user {} as they don't accept DMs.", userId);
                            informDeletion(user.getUser(), guild, pings);
                            CustomPings.clearPings(guild.getIdLong(), userId);
                        })));
                }, $ -> /* Can't DM, so clear pings */ CustomPings.clearPings(guild.getIdLong(), userId));
            }, e -> /* User left the guild */ CustomPings.clearPings(guild.getIdLong(), userId));
        });
    }

    public static MessageCreateAction sendPingMessage(final CustomPing ping, final Message message, final MessageChannel channel) {
        return channel.sendMessageEmbeds(
            new EmbedBuilder()
                .setAuthor("New ping from: %s".formatted(message.getAuthor().getName()), message.getJumpUrl(), message.getAuthor().getAvatarUrl())
                .addField(ping.text(), Utils.truncate(message.getContentRaw().isBlank() ? "[Blank]" : message.getContentRaw(), MessageEmbed.VALUE_MAX_LENGTH), false)
                .addField("Link", message.getJumpUrl(), false)
                .setTimestamp(message.getTimeCreated())
                .build()
        );
    }

    public static boolean canViewChannel(Member member, GuildChannel channel) {
        return member.getPermissions(channel).contains(Permission.VIEW_CHANNEL);
    }

    private static void informDeletion(User user, Guild guild, List<CustomPing> pings) {
        final MessageChannel channel = TheCommander.getInstance().getConfigForGuild(guild).features()
            .customPings().removalInformChannel().resolve(it -> guild.getChannelById(MessageChannel.class, it));

        if (channel != null) {
            final String asString = CustomPings.toString(pings);

            channel.sendMessage(user.getAsMention() + ", your custom pings have been removed as I cannot DM you. Use the buttons below to restore or see your old pings.")
                .setActionRow(
                    COMPONENTS.createButton(
                        ButtonStyle.SECONDARY, "\uD83D\uDD3D List", null, Component.Lifespan.PERMANENT, List.of(
                            user.getId(), "list-removed", asString
                        )
                    ),
                    COMPONENTS.createButton(
                        ButtonStyle.SECONDARY, "↩ Restore", null, Component.Lifespan.PERMANENT, List.of(
                            user.getId(), "restore-removed", asString
                        )
                    )
                )
                .queue();
        }
    }

    private static void onListRemovedPings(final ButtonInteractionContext context) {
        final List<CustomPing> pings = CustomPings.fromString(context.getArguments().get(0));

        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Your removed custom pings:");

        for (int i = 0; i < pings.size(); i++) {
            final var ping = pings.get(i);

            if (i > 0) embed.appendDescription(System.lineSeparator());
            embed.appendDescription("%s) `%s` | %s".formatted(i, ping.pattern(), ping.text()));
        }

        context.getEvent().replyEmbeds(embed.build())
            .setEphemeral(true).queue();
    }

    private static void onRestoreRemovedPings(final ButtonInteractionContext context) {
        CustomPings.addPings(
            context.getGuild().getIdLong(),
            context.getUser().getIdLong(),
            CustomPings.fromString(context.getArguments().get(0))
        );

        context.getEvent().reply("Restored removed pings!")
            .setEphemeral(true).queue();

        context.deleteComponent();
    }
}
