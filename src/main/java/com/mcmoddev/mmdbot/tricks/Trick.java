package com.mcmoddev.mmdbot.tricks;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public interface Trick {
    List<String> getNames();
    Message getMessage(String[] args);
}
