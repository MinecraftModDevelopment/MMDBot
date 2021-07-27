package com.mcmoddev.mmdbot.utilities.tricks;

import com.google.common.collect.Lists;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.List;

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
        return new MessageBuilder(getBody()).build();
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
         * Gets arg names.
         *
         * @return arg names
         */
        @Override
        public List<String> getArgNames() {
            return Lists.newArrayList("names", "body");
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
    }
}
