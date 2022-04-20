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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The type String trick.
 *
 * @author williambl
 */
public record StringTrick(List<String> names, String body) implements Trick {
    public static final TrickType<StringTrick> TYPE = new Type();

    /**
     * Gets names.
     *
     * @return Name of the trick.
     */
    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public void execute(final TrickContext context) {
        context.replyWithMessage(new MessageBuilder(String.format(getBody(), (Object[]) context.getArgs()))
            .setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build());
    }

    /**
     * Gets body.
     *
     * @return body
     */
    public String getBody() {
        return body;
    }

    @Override
    public String getRaw() {
        return getBody();
    }

    @Override
    public TrickType<?> getType() {
        return TYPE;
    }

    /**
     * The type Type.
     */
    public static class Type implements TrickType<StringTrick> {

        public static final Codec<StringTrick> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("names").forGetter(StringTrick::names),
            Codec.STRING.fieldOf("body").forGetter(StringTrick::body)
        ).apply(instance, StringTrick::new));

        private Type() {

        }

        /**
         * Gets clazz.
         *
         * @return clazz
         */
        @Override
        public Class<StringTrick> getClazz() {
            return StringTrick.class;
        }

        /**
         * Create from args string trick.
         *
         * @param args the args
         * @return string trick
         */
        @Override
        public StringTrick createFromArgs(final String args) {
            String[] argsArray = args.split(" \\| ");
            return new StringTrick(Arrays.asList(argsArray[0].split(" ")), argsArray[1]);
        }

        private static final List<String> ARG_NAMES = List.of("names", "content");

        @Override
        public List<String> getArgNames() {
            return ARG_NAMES;
        }

        private static final LazySupplier<List<ActionRow>> MODAL_ARGS = LazySupplier.of(() -> {
            final var names = TextInput.create("names", "Names", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(1, TextInput.MAX_VALUE_LENGTH)
                .setPlaceholder("Name(s) for the trick. Separate with spaces.")
                .build();

            final var content = TextInput.create("content", "Content", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(1, TextInput.MAX_VALUE_LENGTH)
                .setPlaceholder("The content of the trick.")
                .build();

            return List.of(ActionRow.of(names), ActionRow.of(content));
        });

        @Override
        public List<ActionRow> getModalArguments() {
            return MODAL_ARGS.get();
        }

        @Override
        public StringTrick createFromModal(final ModalInteractionEvent event) {
            return new StringTrick(Arrays.asList(event.getValue("names").getAsString()
                .split(" ")), event.getValue("content").getAsString());
        }

        @Override
        public Codec<StringTrick> getCodec() {
            return CODEC;
        }
    }
}
