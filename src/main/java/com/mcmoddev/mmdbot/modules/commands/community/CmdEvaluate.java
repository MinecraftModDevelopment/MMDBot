package com.mcmoddev.mmdbot.modules.commands.community;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils;
import com.mcmoddev.mmdbot.utilities.tricks.TrickContext;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class CmdEvaluate extends SlashCommand {

    public CmdEvaluate() {
        guildOnly = true;
        name = "evaluate";
        aliases = new String[] {"eval"};
        help = "Evaluates the given script";
        options = List.of(new OptionData(OptionType.STRING, "script", "The script to evaluate.").setRequired(true));
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        event.deferReply().queue(hook -> ScriptingUtils.evaluate(event.getOption("script").getAsString(),
            ScriptingUtils.createTrickContext(new TrickContext.Slash(event, hook, new String[0]))));
    }

    @Override
    protected void execute(final CommandEvent event) {
        ScriptingUtils.evaluate(event.getArgs(), ScriptingUtils.createTrickContext(new TrickContext.Normal(event, new String[0])));
    }
}
