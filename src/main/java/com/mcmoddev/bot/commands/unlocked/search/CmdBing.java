package com.mcmoddev.bot.commands.unlocked.search;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.entities.TextChannel;

public class CmdBing extends Command {

    public CmdBing() {
        name = "bing";
        help = "Looking for something? Bing! I found it!";
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel channel = event.getTextChannel();
        String searchTerm = event.getMessage().getContentRaw().toLowerCase().replace(BotConfig.getConfig().getPrefix() + "bing ", "");
        String searchQuery = "<https://www.bing.com/search?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}