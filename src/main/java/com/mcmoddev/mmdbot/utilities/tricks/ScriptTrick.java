package com.mcmoddev.mmdbot.utilities.tricks;

import static com.mcmoddev.mmdbot.utilities.Utils.getOrEmpty;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.List;

public final class ScriptTrick implements Trick {

    /**
     * The Names.
     */
    private final List<String> names;

    /**
     * The Body.
     */
    private final String script;

    public ScriptTrick(final List<String> names, final String script) {
        this.names = names;
        this.script = script;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public void execute(final TrickContext context) {
        try {
            ScriptingUtils.evaluate(script, ScriptingUtils.createTrickContext(context));
        } catch (ScriptingUtils.ScriptingException e) {
            context.reply("There was an exception executing the script: %s".formatted(e.getLocalizedMessage()));
        }
    }

    @Override
    public String getRaw() {
        return script;
    }

    static class Type implements TrickType<ScriptTrick> {

        @Override
        public Class<ScriptTrick> getClazz() {
            return ScriptTrick.class;
        }

        @Override
        public ScriptTrick createFromArgs(final String args) {
            String[] argsArray = args.split(" \\| ", 2);
            return new ScriptTrick(Arrays.asList(argsArray[0].split(" ")), argsArray[1]);
        }

        @Override
        public List<OptionData> getArgs() {
            return List.of(
                new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.").setRequired(true),
                new OptionData(OptionType.STRING, "script", "The script of the trick.").setRequired(true)
            );
        }

        @Override
        public ScriptTrick createFromCommand(final SlashCommandEvent event) {
            return new ScriptTrick(Arrays.asList(getOrEmpty(event, "names").split(" ")), getOrEmpty(event, "script"));
        }
    }
}
