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
package com.mcmoddev.mmdbot.commander.commands.tricks;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
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
import net.dv8tion.jda.api.interactions.components.Modal;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Edits an already-existing trick.
 * <p>
 * Has two subcommands;
 * - string;
 * - Takes one parameter - the content of the string trick.
 * - embed;
 * - Takes three parameters; name, description and color. All used for constructing the embed.
 *
 * @author Will BL
 * @author Curle
 */
public final class EditTrickCommand extends SlashCommand {

    /**
     * Instantiates a new edit trick command.
     */
    public EditTrickCommand() {
        super();
        name = "edit";
        help = "Edits/replaces a trick. Similar in usage to /trick add.";
        category = new Category("Info");
        arguments = "(<string> <trick content body> (or) <embed> <title> "
            + "<description> <colour-as-hex-code>";
        enabledRoles = AddTrickCommand.BOT_MAINTAINERS_GETTER.get();
        guildOnly = true;
        // we need to use this unfortunately :( can't create more than one commandclient
        //guildId = Long.toString(MMDBot.getConfig().getGuildID());

        children = Tricks.getTrickTypes().entrySet().stream().map(entry -> new SubCommand(entry.getKey(), entry.getValue())).toArray(SlashCommand[]::new);
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
    }

    /**
     * A child command of EditTrick, handles adding a particular type of trick.
     *
     * @author Will BL
     */
    private static class SubCommand extends SlashCommand {
        private final String trickTypeName;
        private final Trick.TrickType<?> trickType;

        public SubCommand(String name, Trick.TrickType<?> trickType) {
            this.trickTypeName = name;
            this.trickType = trickType;
            this.name = name;
            this.help = "Edits %s %s-type trick.".formatted(StringUtilities.startWithVowel(name) ? "an" : "a", name);
            this.guildOnly = true;
        }

        @Override
        protected void execute(final SlashCommandEvent event) {
            final var modal = Modal.create(ModalListener.MODAL_ID_PREFIX + trickTypeName, "Edit %s %s trick".formatted(StringUtilities.startWithVowel(trickTypeName) ? "an" : "a", trickTypeName))
                .addActionRows(trickType.getModalArguments())
                .build();
            event.replyModal(modal).queue();
        }
    }

    public static final class ModalListener extends ListenerAdapter {
        public static final String MODAL_ID_PREFIX = "edittrick_";

        public ModalListener() {}

        @Override
        public void onModalInteraction(@NotNull final ModalInteractionEvent event) {
            if (!event.getModalId().startsWith(MODAL_ID_PREFIX)) return;
            final var trickTypeStr = event.getModalId().replace(MODAL_ID_PREFIX, "");
            final var type = Tricks.getTrickType(trickTypeStr);
            if (type != null) {
                Trick trick = type.createFromModal(event);
                Optional<Trick> originalTrick = Tricks.getTricks().stream()
                    .filter(t -> t.getNames().stream().anyMatch(n -> trick.getNames().contains(n))).findAny();

                originalTrick.ifPresentOrElse(
                    old -> {
                        Tricks.replaceTrick(old, trick);
                        event.reply("Updated trick!").mentionRepliedUser(false).setEphemeral(true).queue();
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
                    },
                    () ->
                        event.reply("No trick with that name exists!").mentionRepliedUser(false).setEphemeral(true).queue()
                );
            } else {
                event.reply("Unknown trick type: **%s**".formatted(trickTypeStr)).queue();
            }
        }
    }

    public static final class Prefix extends Command {
        public Prefix() {
            super();
            name = "edittrick";
            help = "Edits/replaces a trick. Similar in usage to !addtrick.";
            category = new Category("Info");
            aliases = new String[]{"edit-trick"};
            guildOnly = true;
            children = Tricks.getTrickTypes().entrySet().stream().map(entry -> new PrefixSubCmd(entry.getKey(), entry.getValue())).toArray(Command[]::new);
        }

        @Override
        protected void execute(final CommandEvent event) {

        }
    }

    private static final class PrefixSubCmd extends Command {
        private final String trickTypeName;
        private final Trick.TrickType<?> trickType;

        public PrefixSubCmd(String name, Trick.TrickType<?> trickType) {
            this.trickTypeName = name;
            this.trickType = trickType;
            this.name = name;
            this.help = "Edits %s %s-type trick.".formatted(StringUtilities.startWithVowel(name) ? "an" : "a", name);
            this.guildOnly = true;
        }

        @Override
        protected void execute(final CommandEvent event) {
            if (!event.getChannel().getType().isGuild()) {
                event.reply("This command only works in guilds!");
                return;
            }

            if (!TheCommanderUtilities.memberHasRoles(event.getMember(), AddTrickCommand.BOT_MAINTAINERS_GETTER.get())) {
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

            Trick trick = trickType.createFromArgs(args);
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
            }, () -> event.getMessage().reply("No command with that name exists!").mentionRepliedUser(false).queue());
        }
    }
}
