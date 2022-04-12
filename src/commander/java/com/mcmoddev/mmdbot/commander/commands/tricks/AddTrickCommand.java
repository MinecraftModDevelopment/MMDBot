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
package com.mcmoddev.mmdbot.commander.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.tricks.ScriptTrick;
import com.mcmoddev.mmdbot.commander.tricks.Trick;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.event.customlog.TrickEvent;
import com.mcmoddev.mmdbot.core.util.StringUtilities;
import com.mcmoddev.mmdbot.core.util.gist.GistUtils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.Modal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * Adds a trick to the list.
 * <p>
 * Has two subcommands;
 * - string;
 * - Takes one parameter - the content of the string trick.
 * - embed;
 * - Takes three parameters; name, description and color. All used for constructing the embed.
 *
 * @author Will BL
 * @author Curle
 * @author matyrobbrt
 */
public final class AddTrickCommand extends SlashCommand {

    public static final Supplier<String[]> BOT_MAINTAINERS_GETTER = () -> TheCommander.getInstance().getGeneralConfig().roles().getBotMaintainers().toArray(String[]::new);
    public static final SubcommandGroupData GROUP = new SubcommandGroupData("add", "Adds a trick.");

    private final String trickTypeName;
    private final Trick.TrickType<?> trickType;

    public AddTrickCommand(String name, Trick.TrickType<?> trickType) {
        this.trickTypeName = name;
        this.trickType = trickType;
        this.name = name;
        subcommandGroup = GROUP;
        this.help = "Add or edit %s %s-type trick.".formatted(StringUtilities.startWithVowel(name) ? "an" : "a", name);
        this.guildOnly = true;
        enabledRoles = BOT_MAINTAINERS_GETTER.get();
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command only works in a guild!").queue();
            return;
        }

        if (!TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
            event.deferReply(true).setContent("Tricks are not enabled!").queue();
            return;
        }

        if (!TheCommanderUtilities.memberHasRoles(event.getMember(), BOT_MAINTAINERS_GETTER.get())) {
            event.deferReply(true).setContent("Only Bot Maintainers can use this command.").queue();
            return;
        }

        final var modal = Modal.create(ModalListener.MODAL_ID_PREFIX + trickTypeName, "Create %s %s trick".formatted(StringUtilities.startWithVowel(trickTypeName) ? "an" : "a", trickTypeName))
            .addActionRows(trickType.getModalArguments())
            .build();
        event.replyModal(modal).queue();
    }

    public static final class ModalListener extends ListenerAdapter {
        public static final String MODAL_ID_PREFIX = "addtrick_";

        public ModalListener() {
        }

        @Override
        public void onModalInteraction(@NotNull final ModalInteractionEvent event) {
            if (!event.getModalId().startsWith(MODAL_ID_PREFIX) || !event.isFromGuild()) return;
            final var trickTypeStr = event.getModalId().replace(MODAL_ID_PREFIX, "");
            final var type = Tricks.getTrickType(trickTypeStr);
            if (type != null) {
                final var trick = type.createFromModal(event);
                Optional<Trick> originalTrick = Tricks.getTricks().stream()
                    .filter(t -> t.getNames().stream().anyMatch(n -> trick.getNames().contains(n))).findAny();

                originalTrick.ifPresentOrElse(old -> {
                    Tricks.replaceTrick(old, trick);
                    event.reply("Updated trick!").mentionRepliedUser(false).queue();
                    Events.CUSTOM_AUDIT_LOG_BUS.post(new TrickEvent.Edit(
                        // Old stuff
                        event.getGuild().getIdLong(),
                        event.getMember().getIdLong(),
                        Tricks.getTrickTypeName(old.getType()),
                        old.getNames(),
                        old.getRaw(),

                        // New stuff
                        trickTypeStr,
                        trick.getNames(),
                        trick.getRaw()
                    ));
                }, () -> {
                    Tricks.addTrick(trick);
                    event.reply("Added trick!").mentionRepliedUser(false).queue();
                    Events.CUSTOM_AUDIT_LOG_BUS.post(new TrickEvent.Add(
                        event.getGuild().getIdLong(),
                        event.getMember().getIdLong(),
                        trickTypeStr,
                        trick.getNames(),
                        trick.getRaw()
                    ));
                });
            } else {
                event.reply("Unknown trick type: **%s**".formatted(trickTypeStr)).queue();
            }
        }
    }

    public static final class Prefix extends Command {

        public Prefix() {
            name = "addtrick";
            aliases = new String[]{"add-trick"};
            requiredRole = "Bot Maintainer";
            guildOnly = true;
            children = Tricks.getTrickTypes().entrySet().stream().map(e -> new PrefixSubCmd(e.getKey(), e.getValue())).toArray(Command[]::new);
        }

        @Override
        protected void execute(final CommandEvent event) {

        }
    }

    private static final class PrefixSubCmd extends Command {

        private final String trickTypeName;
        private final Trick.TrickType<?> trickType;

        private PrefixSubCmd(final String name, final Trick.TrickType<?> trickType) {
            this.trickType = trickType;
            this.name = name;
            this.trickTypeName = name;
        }

        @Override
        protected void execute(final CommandEvent event) {
            if (!event.getChannel().getType().isGuild()) {
                event.reply("This command only works in guilds!");
                return;
            }

            if (!TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
                event.reply("Tricks are not enabled!");
                return;
            }

            if (!TheCommanderUtilities.memberHasRoles(event.getMember(), BOT_MAINTAINERS_GETTER.get())) {
                event.reply("Only Bot Maintainers can use this command.");
                return;
            }

            var args = event.getArgs();

            if (trickType instanceof ScriptTrick.Type && !event.getMessage().getAttachments().isEmpty()) {
                for (var attach : event.getMessage().getAttachments()) {
                    if (Objects.equals(attach.getFileExtension(), "js")) {
                        try {
                            args = event.getArgs().split(" \\| ", 2)[0] + " | " + GistUtils.readInputStream(attach.retrieveInputStream().get());
                            break;
                        } catch (IOException | InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            final var trick = trickType.createFromArgs(args);
            Optional<Trick> originalTrick = Tricks.getTricks().stream()
                .filter(t -> t.getNames().stream().anyMatch(n -> trick.getNames().contains(n))).findAny();

            originalTrick.ifPresentOrElse(old -> {
                Tricks.replaceTrick(old, trick);
                event.getMessage().reply("Updated trick!").mentionRepliedUser(false).queue();
                Events.CUSTOM_AUDIT_LOG_BUS.post(new TrickEvent.Edit(
                    // Old stuff
                    event.getGuild().getIdLong(),
                    event.getMember().getIdLong(),
                    Tricks.getTrickTypeName(old.getType()),
                    old.getNames(),
                    old.getRaw(),

                    // New stuff
                    trickTypeName,
                    trick.getNames(),
                    trick.getRaw()
                ));
            }, () -> {
                Tricks.addTrick(trick);
                event.getMessage().reply("Added trick!").mentionRepliedUser(false).queue();
                Events.CUSTOM_AUDIT_LOG_BUS.post(new TrickEvent.Add(
                    event.getGuild().getIdLong(),
                    event.getMember().getIdLong(),
                    trickTypeName,
                    trick.getNames(),
                    trick.getRaw()
                ));
            });
        }
    }

}
