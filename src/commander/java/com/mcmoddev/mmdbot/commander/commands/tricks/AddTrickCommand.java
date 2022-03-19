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
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.tricks.ScriptTrick;
import com.mcmoddev.mmdbot.commander.tricks.Trick;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;
import com.mcmoddev.mmdbot.core.util.gist.GistUtils;

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
    private final Trick.TrickType<?> trickType;

    public AddTrickCommand(String name, Trick.TrickType<?> trickType) {
        this.trickType = trickType;
        this.name = "create-" + name;
        this.help = "Add or create a " + name + "-type trick.";
        this.guildOnly = true;
        this.options = trickType.getArgs();
        enabledRoles = BOT_MAINTAINERS_GETTER.get();
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command only works in a guild!").queue();
            return;
        }

        if (TheCommander.getInstance().getGeneralConfig().features().tricks().tricksEnabled()) {
            event.deferReply(true).setContent("Tricks are not enabled!").queue();
            return;
        }

        if (!TheCommanderUtilities.memberHasRoles(event.getMember(), enabledRoles)) {
            event.deferReply(true).setContent("Only Bot Maintainers can use this command.").queue();
            return;
        }

        Trick trick = trickType.createFromCommand(event);
        Optional<Trick> originalTrick = Tricks.getTricks().stream()
            .filter(t -> t.getNames().stream().anyMatch(n -> trick.getNames().contains(n))).findAny();

        originalTrick.ifPresentOrElse(original -> {
            Tricks.replaceTrick(original, trick);
            event.reply("Updated trick!").mentionRepliedUser(false).setEphemeral(true).queue();
        }, () -> {
            Tricks.addTrick(trick);
            event.reply("Added trick!").mentionRepliedUser(false).setEphemeral(true).queue();
        });
    }

    public static final class Prefix extends Command {

        public Prefix() {
            name = "addtrick";
            arguments = "(<string> <trick content body> (or) <embed> <title> "
                + "<description> <colour-as-hex-code>";
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

        private final Trick.TrickType<?> trickType;

        private PrefixSubCmd(final String name, final Trick.TrickType<?> trickType) {
            this.trickType = trickType;
            this.name = name;
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

            Tricks.addTrick(trickType.createFromArgs(args));
            event.getMessage().reply("Added trick!").mentionRepliedUser(false).queue();
        }
    }
}
