package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.commands.staff.CmdUser;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
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
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        final TextChannel channel = event.getTextChannel();
        final EmbedBuilder embed = createMemberEmbed(event.getMember());
        channel.sendMessage(embed.build()).queue();
    }
}
