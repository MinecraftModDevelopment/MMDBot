package com.mcmoddev.bot.commands.unlocked.search;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.entities.TextChannel;

public class CmdDuckDuckGo extends Command {

    public CmdDuckDuckGo() {
        name = "duckduckgo";
        help = "Search for something with a bit more privacy using Duck Duck Go";
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel channel = event.getTextChannel();
        String searchTerm = event.getMessage().getContentRaw().toLowerCase().replace(BotConfig.getConfig().getPrefix() + "duckduckgo ", "");
        String searchQuery = "<https://duckduckgo.com/?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}
