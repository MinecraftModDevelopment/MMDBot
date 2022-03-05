package com.mcmoddev.mmdbot.commander.commands.curseforge;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.cfwebhooks.CurseForgeManager;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;

import javax.annotation.Nullable;

public abstract class CurseForgeCommand extends SlashCommand {

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
     * @param event the context
     * @param manager the CF manager
     */
    protected abstract void execute(final CFCommandContext context, CurseForgeManager manager) throws CurseForgeException;

    protected interface CFCommandContext {
        @Nullable Member getMember();

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
