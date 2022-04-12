/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.commander.tricks;

import com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils;
import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.Arrays;
import java.util.List;

public record ScriptTrick(List<String> names, String script) implements Trick {
    public static final TrickType<ScriptTrick> TYPE = new Type();

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

    @Override
    public TrickType<?> getType() {
        return TYPE;
    }

    public static final class Type implements TrickType<ScriptTrick> {
        private Type() {
        }

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

        private static final List<String> ARG_NAMES = List.of("names", "script");

        @Override
        public List<String> getArgNames() {
            return ARG_NAMES;
        }

        private static final LazySupplier<List<ActionRow>> MODAL_ARGS = LazySupplier.of(() -> {
            final var names = TextInput.create("names", "Names", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(1, TextInput.TEXT_INPUT_MAX_LENGTH)
                .setPlaceholder("Name(s) for the trick. Separate with spaces.")
                .build();

            final var script = TextInput.create("script", "Script", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(1, TextInput.TEXT_INPUT_MAX_LENGTH)
                .setPlaceholder("The script of the trick.")
                .build();

            return List.of(ActionRow.of(names), ActionRow.of(script));
        });

        @Override
        public List<ActionRow> getModalArguments() {
            return MODAL_ARGS.get();
        }

        @Override
        public ScriptTrick createFromModal(final ModalInteractionEvent event) {
            return new ScriptTrick(Arrays.asList(event.getValue("names").getAsString()
                .split(" ")), event.getValue("script").getAsString());
        }
    }
}
