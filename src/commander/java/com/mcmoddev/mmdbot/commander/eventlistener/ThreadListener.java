package com.mcmoddev.mmdbot.commander.eventlistener;

import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public final class ThreadListener extends ListenerAdapter {

    @Override
    public void onChannelCreate(@NotNull final ChannelCreateEvent event) {
        if (event.getChannel() instanceof ThreadChannel thread) {
            thread.addThreadMember(event.getJDA().getSelfUser()).queue();
        }
    }
}
