/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.mcmoddev.mmdbot.utilities.Utils.getOrEmpty;

/**
 * The type String trick.
 *
 * @author williambl
 */
public class StringTrick implements Trick {

    /**
     * The Names.
     */
    private final List<String> names;

    /**
     * The Body.
     */
    private final String body;

    /**
     * Instantiates a new String trick.
     *
     * @param names the names
     * @param body  the body
     */
    public StringTrick(final List<String> names, final String body) {
        this.names = names;
        this.body = body;
    }

    /**
     * Gets names.
     *
     * @return Name of the trick.
     */
    @Override
    public List<String> getNames() {
        return names;
    }

    /**
     * Gets message.
     *
     * @param args the args
     * @return message
     */
    @Override
    public Message getMessage(final String[] args) {
        return new MessageBuilder(String.format(getBody(), (Object[]) args)).setAllowedMentions(Set.of(Message.MentionType.CHANNEL, Message.MentionType.EMOTE)).build();
    }

    /**
     * Gets body.
     *
     * @return body body
     */
    public String getBody() {
        return body;
    }

    /**
     * The type Type.
     */
    static class Type implements TrickType<StringTrick> {

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

        @Override
        public List<OptionData> getArgs() {
            return List.of(
                new OptionData(OptionType.STRING, "names", "Name(s) for the trick. Separate with spaces.").setRequired(true),
                new OptionData(OptionType.STRING, "content", "The content of the trick.").setRequired(true)
            );
        }

        @Override
        public StringTrick createFromCommand(final SlashCommandEvent event) {
            return new StringTrick(Arrays.asList(getOrEmpty(event, "names").split(" ")), getOrEmpty(event, "content"));
        }
    }
}
