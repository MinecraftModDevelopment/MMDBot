package com.mcmoddev.bot.commands.unlocked.search;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.entities.TextChannel;

public class CmdLmgtfy extends Command {

    public CmdLmgtfy() {
        name = "lmgtfy";
        help = "Assist someone of the restful variety in searching for something.";
    }

    @Override
    protected void execute(CommandEvent event) {
        TextChannel channel = event.getTextChannel();
        String searchTerm = event.getMessage().getContentRaw().toLowerCase().replace(BotConfig.getConfig().getPrefix() + "google ", "");
        String searchQuery = "<http://lmgtfy.com/?q=" + searchTerm.replace(" ", "+") + ">";

        channel.sendMessage(searchQuery).queue();
    }
}
