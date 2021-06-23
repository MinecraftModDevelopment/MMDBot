package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.commands.staff.CmdUser;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * @author ProxyNeko
 * @author sciwhiz12
 */
public final class CmdMe extends CmdUser {

    /**
     *
     */
    public CmdMe() {
        super();
        name = "me";
        aliases = new String[]{"whoami", "myinfo"};
        help = "Get information about your own user.";
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var channel = event.getTextChannel();
        final EmbedBuilder embed = createMemberEmbed(event.getMember());
        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
