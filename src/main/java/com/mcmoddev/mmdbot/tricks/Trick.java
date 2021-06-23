package com.mcmoddev.mmdbot.tricks;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author williambl
 */
public interface Trick {
    List<String> getNames();
    Message getMessage(String[] args);

    interface TrickType<T extends Trick> {
        Class<T> getClazz();
        List<String> getArgNames();
        T createFromArgs(String args);
    }
}
