package com.mcmoddev.mmdbot.tricks;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;

public class StringTrick implements Trick {
    private final List<String> names;
    private final String body;

    public StringTrick(final List<String> names, final String body) {
        this.names = names;
        this.body = body;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public Message getMessage(final String[] args) {
        return new MessageBuilder(getBody()).build();
    }

    public String getBody() {
        return body;
    }

    static class Type implements TrickType<StringTrick> {
        @Override
        public Class<StringTrick> getClazz() {
            return StringTrick.class;
        }

        @Override
        public List<String> getArgNames() {
            return Lists.newArrayList("names", "body");
        }

        @Override
        public StringTrick createFromArgs(final String args) {
            String[] argsArray = args.split(" \\| ");
            return new StringTrick(Arrays.asList(argsArray[0].split(" ")), argsArray[1]);
        }
    }
}
