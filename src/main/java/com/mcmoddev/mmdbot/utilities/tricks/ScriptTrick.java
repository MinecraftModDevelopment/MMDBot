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
package com.mcmoddev.mmdbot.utilities.tricks;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.utilities.scripting.ScriptingUtils;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.List;

import static com.mcmoddev.mmdbot.utilities.Utils.getOrEmpty;

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
            var script = argsArray[1];
            if (script.contains("```js") && script.endsWith("```")) {
                script = script.substring(script.indexOf("```js") + 5);
                script = script.substring(0, script.lastIndexOf("```"));
            }
            return new ScriptTrick(Arrays.asList(argsArray[0].split(" ")), script);
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
