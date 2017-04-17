package com.mcmoddev.bot.handlers;

import com.mcmoddev.bot.util.MMDRole;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Thanks to MihaiC for providing the zalgo char codes. http://stackoverflow.com/a/26927946
 */
public class TableHandler {

    @EventSubscriber
    public void onMessageRecieved (MessageReceivedEvent event) {

        final IMessage message = event.getMessage();

        if (MMDRole.ADMIN.hasRole(event.getAuthor()))
            return;

        if (message.getContent().contains("┻━┻")) {

            message.delete();

            Utilities.sendPrivateMessage(event.getAuthor(), "┬─┬﻿ ノ( ゜-゜ノ)");
        }
    }
}
