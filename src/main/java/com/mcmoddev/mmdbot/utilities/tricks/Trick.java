package com.mcmoddev.mmdbot.utilities.tricks;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * The interface Trick.
 *
 * @author williambl
 */
public interface Trick {

    /**
     * Gets names.
     *
     * @return Get trick names.
     */
    List<String> getNames();

    /**
     * Gets message.
     *
     * @param args the args
     * @return message message
     */
    Message getMessage(String[] args);

    /**
     * The interface Trick type.
     *
     * @param <T> the type parameter
     */
    interface TrickType<T extends Trick> {

        /**
         * Gets clazz.
         *
         * @return clazz clazz
         */
        Class<T> getClazz();

        /**
         * Gets arg names.
         *
         * @return arg names
         */
        List<String> getArgNames();

        /**
         * Create from args t.
         *
         * @param args the args
         * @return t t
         */
        T createFromArgs(String args);
    }
}
