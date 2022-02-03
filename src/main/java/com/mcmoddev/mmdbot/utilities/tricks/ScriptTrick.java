package com.mcmoddev.mmdbot.utilities.tricks;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public record ScriptTrick(List<String> names, String script) implements Trick {

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public Message getMessage(final String[] args) {
        return null;
    }

    static class Type implements TrickType<ScriptTrick> {

        @Override
        public Class<ScriptTrick> getClazz() {
            return ScriptTrick.class;
        }

        @Override
        public ScriptTrick createFromArgs(final String args) {
            return null;
        }

        @Override
        public List<OptionData> getArgs() {
            return List.of();
        }

        @Override
        public ScriptTrick createFromCommand(final SlashCommandEvent event) {
            return null;
        }
    }
}
