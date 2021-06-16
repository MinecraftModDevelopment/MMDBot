package com.mcmoddev.mmdbot.commands.info.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

/**
 *
 * @author
 *
 */
public final class CmdGreatMoves extends Command {

	/**
	 *
	 */
    public static final String URL = "https://soundcloud.com/aldenchambers/great-moves-keep-it-up";

    /**
     *
     */
    public CmdGreatMoves() {
        name = "greatmoves";
        aliases = new String[]{"great-moves"};
        help = "Posts an encouraging message in chat.";
        guildOnly = false;
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        event.reply("Great Moves! Keep it Up! Proud of ya! Papa bless!" + '\n' + URL);
    }
}
