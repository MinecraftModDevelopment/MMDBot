package com.mcmoddev.bot.commands.unlocked.search;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.entities.TextChannel;

public class CmdGoogle extends Command {

    public CmdGoogle() {
        name = "google";
        help = "Google something rather than load a browser manually then Google it.";
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel channel = event.getTextChannel();
        String searchTerm = event.getMessage().getContentRaw().toLowerCase().replace(BotConfig.getConfig().getPrefix() + "google ", "");
        String searchQuery = "<https://google.com/search?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}
