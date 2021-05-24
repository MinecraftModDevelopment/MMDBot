package com.mcmoddev.mmdbot.tricks;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.Arrays;
import java.util.List;

public class EmbedTrick implements Trick {
    private final List<String> names;
    private final String title;
    private final String description;
    private final int color;
    private final List<MessageEmbed.Field> fields;

    public EmbedTrick(final List<String> names, final String title, final String description, final int color, final MessageEmbed.Field... fields) {
        this.names = names;
        this.title = title;
        this.description = description;
        this.color = color;
        this.fields = Arrays.asList(fields);
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public Message getMessage(final String[] args) {
        EmbedBuilder builder = new EmbedBuilder()
            .setTitle(getTitle())
            .setDescription(getDescription())
            .setColor(color);
        for (MessageEmbed.Field field : getFields()) {
            builder.addField(field);
        }
        return new MessageBuilder(builder.build()).build();
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }

    public List<MessageEmbed.Field> getFields() {
        return fields;
    }

    private static class Type implements TrickType<EmbedTrick> {
        @Override
        public Class<EmbedTrick> getClazz() {
            return EmbedTrick.class;
        }

        @Override
        public List<String> getArgNames() {
            return Lists.newArrayList("names", "title", "description", "color", "fields");
        }

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

        static {
            Tricks.registerTrickType("embed", new Type());
        }
    }
}
