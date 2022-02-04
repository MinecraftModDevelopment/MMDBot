package com.mcmoddev.mmdbot.modules.commands.community;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingContext;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils;
import com.mcmoddev.mmdbot.utilities.tricks.TrickContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CmdEvaluate extends SlashCommand {

    public CmdEvaluate() {
        guildOnly = true;
        name = "evaluate";
        options = List.of(new OptionData(OptionType.STRING, "script", "The script to evaluate.").setRequired(true));
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        ScriptingUtils.evaluate(event.getOption("script").getAsString(), ScriptingUtils.createTrickContext(new TrickContext() {
            @Nullable
            @Override
            public Member getMember() {
                return event.getMember();
            }

            @NotNull
            @Override
            public User getUser() {
                return event.getUser();
            }

            @NotNull
            @Override
            public TextChannel getChannel() {
                return event.getTextChannel();
            }

            @Nullable
            @Override
            public Guild getGuild() {
                return event.getGuild();
            }

            @NotNull
            @Override
            public String[] getArgs() {
                return new String[0];
            }

            @Override
            public void reply(final String content) {
                event.reply(content).queue();
            }

            @Override
            public void replyEmbeds(final MessageEmbed... embeds) {
                event.replyEmbeds(Arrays.asList(embeds)).queue();
            }
        }));
    }

    @Override
    protected void execute(final CommandEvent event) {
        ScriptingUtils.evaluate(event.getArgs(), ScriptingUtils.createTrickContext(new TrickContext() {
            @Nullable
            @Override
            public Member getMember() {
                return event.getMember();
            }

            @NotNull
            @Override
            public User getUser() {
                return event.getMessage().getAuthor();
            }

            @NotNull
            @Override
            public TextChannel getChannel() {
                return event.getTextChannel();
            }

            @Nullable
            @Override
            public Guild getGuild() {
                return event.getGuild();
            }

            @NotNull
            @Override
            public String[] getArgs() {
                return new String[0];
            }

            @Override
            public void reply(final String content) {
                event.reply(content);
            }

            @Override
            public void replyEmbeds(final MessageEmbed... embeds) {
                event.getMessage().replyEmbeds(Arrays.asList(embeds)).queue();
            }
        }));
    }
}
