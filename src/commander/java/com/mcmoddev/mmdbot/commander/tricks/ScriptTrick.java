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
package com.mcmoddev.mmdbot.commander.tricks;

import com.mcmoddev.mmdbot.commander.util.script.ScriptingUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        public static final Codec<ScriptTrick> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<T> encode(final ScriptTrick input, final DynamicOps<T> ops, final T prefix) {
                return ops.mergeToMap(prefix, Map.of(
                    ops.createString("names"), ops.createList(input.getNames().stream().map(ops::createString)),
                    ops.createString("script"), ops.createString(input.script())
                ));
            }

            @Override
            public <T> DataResult<Pair<ScriptTrick, T>> decode(final DynamicOps<T> ops, final T input) {
                return ops.getMap(input).map(map -> {
                    final var names = new ArrayList<String>();
                    ops.getList(map.get(ops.createString("names"))).get().orThrow().accept(t -> names.add(ops.getStringValue(t).get().orThrow()));
                    return Pair.of(
                        new ScriptTrick(names, ops.getStringValue(map.get(ops.createString("script"))).get().orThrow()),
                    input);
                });
            }
        };

        private Type() {}

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

        @Override
        public Codec<ScriptTrick> getCodec() {
            return CODEC;
        }
    }
}
