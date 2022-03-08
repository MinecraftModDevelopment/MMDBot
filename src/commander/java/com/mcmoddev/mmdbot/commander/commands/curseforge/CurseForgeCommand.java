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
package com.mcmoddev.mmdbot.commander.commands.curseforge;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.cfwebhooks.CurseForgeManager;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;

import javax.annotation.Nullable;

/**
 * The base class for CurseForge commands
 *
 * @author matyrobbrt
 */
public abstract class CurseForgeCommand extends SlashCommand {

    @RegisterSlashCommand
    public static final SlashCommand INSTANCE = SlashCommandBuilder.builder()
        .name("curseforge")
        .help("Commands regarding CurseForge stuff.")
        .build();

    @Override
    protected final void execute(final SlashCommandEvent event) {
        final var managerOpt = TheCommander.getInstance().getCurseForgeManager();
        managerOpt.ifPresentOrElse(manager -> event.deferReply().queue(hook -> {
            try {
                execute(new CFCommandContext() {
                    @Nullable
                    @Override
                    public Member getMember() {
                        return event.getMember();
                    }

                    @Override
                    public @NonNull User getUser() {
                        return event.getUser();
                    }

                    @Override
                    public @NonNull InteractionHook getHook() {
                        return hook;
                    }
                }, manager);
            } catch (CurseForgeException e) {
                hook.editOriginal("Exception while trying to execute that command: %s".formatted(e.getLocalizedMessage())).queue();
                TheCommander.LOGGER.error("Exception while executing a CF command!", e);
            }
        }), () -> event.deferReply(true)
            .setContent("I am not configured with a (valid) CurseForge API key. Please contact the bot owner.")
            .queue());
    }

    /**
     * Executes this command
     *
     * @param context the context
     * @param manager the CF manager
     */
    protected abstract void execute(final CFCommandContext context, CurseForgeManager manager) throws CurseForgeException;

    protected interface CFCommandContext {
        @Nullable
        Member getMember();

        @NonNull User getUser();

        @NonNull InteractionHook getHook();

        default void reply(String content) {
            getHook().editOriginal(content).queue();
        }

        default void replyEmbeds(MessageEmbed... embeds) {
            getHook().editOriginalEmbeds(embeds).queue();
        }
    }
}