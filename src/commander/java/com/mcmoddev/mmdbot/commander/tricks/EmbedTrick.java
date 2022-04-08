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

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.dfu.ExtendedCodec;
import com.mcmoddev.mmdbot.core.dfu.ExtendedDynamicOps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.matyrobbrt.eventdispatcher.LazySupplier;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The type Embed trick.
 *
 * @author williambl
 */
public class EmbedTrick implements Trick {
    public static final TrickType<EmbedTrick> TYPE = new Type();

    /**
     * Name of the trick.
     */
    private final List<String> names;

    /**
     * Trick title.
     */
    private final String title;

    /**
     * The description of the trick.
     */
    private final String description;

    /**
     * The embed edge color for the trick.
     */
    private final int color;

    /**
     * The Fields.
     */
    private final List<MessageEmbed.Field> fields;

    /**
     * Instantiates a new Embed trick.
     *
     * @param names       Name of the trick.
     * @param title       Trick title.
     * @param description The description of the trick.
     * @param color       The embed edge color for the trick.
     * @param fields      the fields
     */
    public EmbedTrick(final List<String> names, final String title, final String description, final int color,
                      final MessageEmbed.Field... fields) {
        this.names = names;
        this.title = title;
        this.description = description;
        this.color = color;
        this.fields = Arrays.asList(fields);
    }

    /**
     * Gets names.
     *
     * @return The names of the tricks.
     */
    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public void execute(final TrickContext context) {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(getTitle())
            .setDescription(getDescription())
            .setColor(color);
        for (MessageEmbed.Field field : getFields()) {
            builder.addField(field);
        }
        context.replyWithMessage(new MessageBuilder(builder.build())
            .setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build());
    }

    /**
     * Gets description.
     *
     * @return Get the trick's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets title.
     *
     * @return Get the title of the trick.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets color.
     *
     * @return Get the embed color for the trick.
     */
    public int getColor() {
        return color;
    }

    @Override
    public String getRaw() {
        return "%s | %s | %s".formatted(getTitle(), getDescription(), getColor());
    }

    /**
     * Gets fields.
     *
     * @return fields fields
     */
    public List<MessageEmbed.Field> getFields() {
        return fields;
    }

    @Override
    public TrickType<?> getType() {
        return TYPE;
    }

    /**
     * The type Type.
     */
    public static class Type implements TrickType<EmbedTrick> {
        public static final Codec<MessageEmbed.Field> FIELD_CODEC = new Codec<>() {
            @Override
            public <T> DataResult<Pair<MessageEmbed.Field, T>> decode(final DynamicOps<T> ops, final T input) {
                return ops.getMap(input).map(map -> Pair.of(new MessageEmbed.Field(
                    ops.getStringValue(map.get(ops.createString("name"))).get().orThrow(),
                    ops.getStringValue(map.get(ops.createString("value"))).get().orThrow(),
                    ops.getBooleanValue(map.get(ops.createString("inline"))).result().orElse(false)
                ), input));
            }

            @Override
            public <T> DataResult<T> encode(final MessageEmbed.Field input, final DynamicOps<T> ops, final T prefix) {
                return ops.mergeToMap(prefix, Map.of(
                    ops.createString("name"), ops.createString(input.getName()),
                    ops.createString("value"), ops.createString(input.getValue()),
                    ops.createString("inline"), ops.createBoolean(input.isInline())
                ));
            }
        };
        public static final Codec<EmbedTrick> CODEC = new ExtendedCodec<>() {
            @Override
            public <T> DataResult<Pair<EmbedTrick, T>> decode(final ExtendedDynamicOps<T> ops, final T input) {
                return ops.getOpsMap(input).map(map -> {
                    final var names = new ArrayList<String>();
                    ops.getList(map.get(ops.createString("names"))).get().orThrow().accept(t -> names.add(ops.getStringValue(t).get().orThrow()));
                    return Pair.of(new EmbedTrick(
                        names,
                        map.getAsString("title").get().orThrow(),
                        map.getAsString("description").get().orThrow(),
                        map.getAsNumber("color").get().orThrow().intValue(),
                        map.getAsListOrThrow("fields", t -> FIELD_CODEC.decode(ops, t).get().orThrow().getFirst(), true).toArray(MessageEmbed.Field[]::new)
                    ), input);
                });
            }

            @Override
            public <T> DataResult<T> encode(final EmbedTrick input, final ExtendedDynamicOps<T> ops, final T prefix) {
                return ops.mergeToMap(prefix, ops.createOpsMap()
                    .put("names", ops.createList(input.getNames().stream().map(ops::createString)))
                    .put("description", ops.createString(input.getDescription()))
                    .put("title", ops.createString(input.getTitle()))
                    .put("color", ops.createInt(input.getColor()))
                    .put("fields", ops.createList(input.getFields().stream()
                        .map(f -> FIELD_CODEC.encodeStart(ops, f)
                        .result()
                        .orElseThrow())))
                );
            }
        };

        private Type() {}

        /**
         * Gets clazz.
         *
         * @return clazz
         */
        @Override
        public Class<EmbedTrick> getClazz() {
            return EmbedTrick.class;
        }

        /**
         * Create from args embed trick.
         *
         * @param args the args
         * @return embed trick
         */
        @Override
        public EmbedTrick createFromArgs(final String args) {
            String[] argsArray = args.split(" \\| ");
            return new EmbedTrick(
                Arrays.asList(argsArray[0].split(" ")),
                argsArray[1],
                argsArray[2],
                Integer.parseInt(argsArray[3].replace("#", ""), 16)
            );
        }

        private static final List<String> ARG_NAMES = List.of("names", "title", "description", "colour");

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

            final var title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setRequired(true)
                .setRequiredRange(1, TextInput.TEXT_INPUT_MAX_LENGTH)
                .setPlaceholder("Title of the embed.")
                .build();

            final var description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setRequired(true)
                .setRequiredRange(1, TextInput.TEXT_INPUT_MAX_LENGTH)
                .setPlaceholder("Description of the embed.")
                .build();

            final var colour = TextInput.create("colour", "Colour", TextInputStyle.SHORT)
                .setRequired(false)
                .setPlaceholder("Hex colour string in #AABBCC format, used for the embed.")
                .build();

            return List.of(ActionRow.of(names), ActionRow.of(title), ActionRow.of(description), ActionRow.of(colour));
        });

        @Override
        public List<ActionRow> getModalArguments() {
            return MODAL_ARGS.get();
        }

        @Override
        public EmbedTrick createFromModal(final ModalInteractionEvent event) {
            final var colour = getOr(event, "colour", "#000000");
            return new EmbedTrick(
                List.of(getOrEmpty(event, "names").split(" ")),
                getOrEmpty(event, "title"),
                getOrEmpty(event, "description"),
                colour.startsWith("#") ? Integer.parseInt(getOrEmpty(event, "colour").replaceAll("#", ""), 16) : Integer.parseInt(colour)
            );
        }

        @Override
        public Codec<EmbedTrick> getCodec() {
            return CODEC;
        }
    }

    private static String getOr(final ModalInteractionEvent event, String id, String or) {
        final var m = event.getValue(id);
        return m == null ? or : m.getAsString();
    }

    private static String getOrEmpty(final ModalInteractionEvent event, String name) {
        return getOr(event, name, "");
    }
}
