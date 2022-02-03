package com.mcmoddev.mmdbot.modules.commands.community;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingContext;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class CmdEvaluate extends SlashCommand {

    public CmdEvaluate() {
        guildOnly = true;
        name = "evaluate";
        options = List.of(new OptionData(OptionType.STRING, "script", "The script to evaluate.").setRequired(true));
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var context = ScriptingContext.of("Event");
        context.set("member", ScriptingUtils.createMember(event.getMember()));
        ScriptingUtils.evaluate(event.getOption("script").getAsString(), context);
    }

    @Override
    protected void execute(final CommandEvent event) {
        final var context = ScriptingContext.of("Event");
        context.set("member", ScriptingUtils.createMember(event.getMember()));
        ScriptingUtils.evaluate(event.getArgs(), context);
    }
}
