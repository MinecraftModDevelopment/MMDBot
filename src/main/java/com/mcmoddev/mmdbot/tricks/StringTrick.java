package com.mcmoddev.mmdbot.tricks;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

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
}
