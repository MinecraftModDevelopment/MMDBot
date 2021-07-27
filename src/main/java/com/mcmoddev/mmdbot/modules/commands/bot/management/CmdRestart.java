package com.mcmoddev.mmdbot.modules.commands.bot.management;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

/**
 * Restart the bot on command rather than via console.
 *
 * @author ProxyNeko
 */
public class CmdRestart extends Command {

    /**
     * Instantiates a new Cmd.
     */
    public CmdRestart() {
        super();
        name = "restart";
        help = "Restarts the bot.";
        ownerCommand = true;
        hidden = true;
        guildOnly = false;
    }

    /**
     * Try to restart the command from Discord rather than having to get someone with actual console access.
     *
     * @param event The event.
     */
    @Override
    protected void execute(final CommandEvent event) {
        //TODO Work on restart code, attempt to make it platform agnostic. -Proxy
    }
}
