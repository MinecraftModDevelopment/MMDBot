package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.modules.logging.misc.ScamDetector;

/**
 * Refreshes the {@link com.mcmoddev.mmdbot.modules.logging.misc.ScamDetector#SCAM_LINKS}
 *
 * @author matyrobbrt
 */
public class CmdRefreshScamLinks extends Command {

    public CmdRefreshScamLinks() {
        name = "refresh-scam-links";
        aliases = new String[]{"refreshscamlinks"};
        help = "Refreshes the scam links";
        category = new Category("Management");
        hidden = true;
        guildOnly = false;
        ownerCommand = true;
    }

    @Override
    protected void execute(final CommandEvent event) {
        event.getMessage().reply("Refreshing scam links...").queue(msg -> {
            new Thread(() -> {
                ScamDetector.setupScamLinks();
                msg.editMessage("Scam links successfully refreshed!").queue();
            }, "Refreshing scam links").start();
        });
    }
}
