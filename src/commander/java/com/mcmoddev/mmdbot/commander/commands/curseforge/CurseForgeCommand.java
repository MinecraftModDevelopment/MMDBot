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
import com.mcmoddev.mmdbot.commander.eventlistener.DismissListener;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.core.util.builder.SlashCommandBuilder;
import com.mcmoddev.mmdbot.core.util.event.OneTimeEventListener;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * The base class for CurseForge commands
 *
 * @author matyrobbrt
 */
public abstract class CurseForgeCommand extends SlashCommand {
    private static final Object2IntMap<String> GAMES = new Object2IntOpenHashMap<>();
    public static final Runnable REFRESH_GAMES_TASK = () -> {
        if (TheCommander.getInstance() == null) {
            return;
        }
        TheCommander.getInstance().getCurseForgeManager().ifPresent(cfManager -> {
            try {
                cfManager.api().getAsyncHelper().getGames().queue(gamesR -> gamesR.ifPresent(games -> {
                    GAMES.clear();
                    games.forEach(g -> GAMES.put(g.name(), g.id()));
                }));
            } catch (Exception e) {
                TheCommander.LOGGER.error("Exception while trying to refresh CurseForge Games!", e);
            }
        });
    };
    public static final OneTimeEventListener<TaskScheduler.CollectTasksEvent> RG_TASK_SCHEDULER_LISTENER = new OneTimeEventListener<>(event -> {
        event.addTask(REFRESH_GAMES_TASK, 0, 1, TimeUnit.HOURS);
    });

    public static Command.Choice[] getGameChoices(String currentGame) {
        return GAMES.object2IntEntrySet().stream()
            .filter(e -> e.getKey().startsWith(currentGame))
            .limit(10)
            .map(e -> new Command.Choice(e.getKey(), e.getIntValue()))
            .toArray(Command.Choice[]::new);
    }

    public static void setEmbedAuthor(EmbedBuilder embed) {
        embed.setAuthor("CurseForge", "https://www.curseforge.com/", "https://th.bing.com/th/id/OIP.d_qkwTnxlcQkqUGsTJkOjAAAAA?pid=ImgDet&rs=1");
    }

    private static final Map<String, SlashCommand> COMMANDS_BY_NAME = new HashMap<>();
    @RegisterSlashCommand
    public static final SlashCommand INSTANCE = SlashCommandBuilder.builder()
        .name("curseforge")
        .help("Commands regarding CurseForge stuff.")
        .children(new CFProjectCommand.Search(), new CFProjectCommand.Info())
        .onAutocomplete(event -> {
            if (event.getSubcommandGroup() != null && event.getSubcommandName() != null) {
                final var cmdName = event.getSubcommandGroup() + "//" + event.getSubcommandName();
                COMMANDS_BY_NAME.get(cmdName).onAutoComplete(event);
            }
        })
        .build();

    static {
        Arrays.stream(INSTANCE.getChildren()).forEach(c -> {
            final var name = c.getSubcommandGroup() == null ? c.getName() : c.getSubcommandGroup().getName() + "//" + c.getName();
            COMMANDS_BY_NAME.put(name, c);
        });
    }

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

                    @Nullable
                    @Override
                    public <T> T getOption(@NotNull final String name, @NotNull final Function<? super OptionMapping, ? extends T> resolver) {
                        return event.getOption(name, resolver);
                    }

                    @Override
                    public <T> T getOption(@NotNull final String name, @Nullable final T fallback, @NotNull final Function<? super OptionMapping, ? extends T> resolver) {
                        return event.getOption(name, fallback, resolver);
                    }
                }, manager);
            } catch (Exception e) {
                hook.editOriginal("Exception while trying to execute that command: %s".formatted(e.getLocalizedMessage())).queue();
                TheCommander.LOGGER.error("Exception while executing a CF command!", e);
            }
        }), () -> event.deferReply(true)
            .setContent("I am not configured with a (valid) CurseForge API key. Please contact the bot owner.")
            .queue());
    }

    @Override
    public final void onAutoComplete(final CommandAutoCompleteInteractionEvent event) {
        try {
            onAutoComplete1(event);
        } catch (CurseForgeException e) {
            TheCommander.LOGGER.error("Exception while autocompleting options for a CF command!", e);
        }
    }

    public void onAutoComplete1(final CommandAutoCompleteInteractionEvent event) throws CurseForgeException {
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

        @Nullable
        <T> T getOption(@Nonnull String name, @Nonnull Function<? super OptionMapping, ? extends T> resolver);

        <T> T getOption(@Nonnull String name,
                        @Nullable T fallback,
                        @Nonnull Function<? super OptionMapping, ? extends T> resolver);

        default void reply(String content) {
            getHook().editOriginal(content)
                .setActionRow(DismissListener.createDismissButton(getUser()))
                .queue();
        }

        default void replyEmbeds(MessageEmbed... embeds) {
            getHook().editOriginalEmbeds(embeds)
                .setActionRow(DismissListener.createDismissButton(getUser()))
                .queue();
        }
    }
}
