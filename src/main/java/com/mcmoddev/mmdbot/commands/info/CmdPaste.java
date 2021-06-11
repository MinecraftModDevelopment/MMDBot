package com.mcmoddev.mmdbot.commands.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.time.Instant;

/**
 *
 * @author
 *
 */
public final class CmdPaste extends Command {

    /**
     *
     */
    private static final String BODY =
        "Please don't upload crash logs and code files to Discord for others, it makes things harder for us and it's also "
            + "risky for the other users who don't know what is in said file." + System.lineSeparator()
            + "Please use one of the great file sharing sites below that let us view the code/logs online without much effort." + System.lineSeparator() + System.lineSeparator()
            + Utils.makeHyperlink("hastebin", "https://hastebin.com") + " " + "Free: 400KB" + System.lineSeparator()
            + Utils.makeHyperlink("hatebin", "https://hatebin.com") + " " + "Free: 50,000 character upload limit." + System.lineSeparator()
            + Utils.makeHyperlink("gist", "https://gist.github.com") + " " + "Free: 100MB Membership required" + System.lineSeparator()
            + Utils.makeHyperlink("paste.ee", "https://paste.ee") + " " + "Free: 1MB - Members get: 6MB" + System.lineSeparator()
            + Utils.makeHyperlink("paste.gg", "https://paste.gg") + " " + "Free: 15MB" + System.lineSeparator()
            + Utils.makeHyperlink("paste.gemwire.uk", "paste.gemwire.uk") + " " + "Free 10MB" + System.lineSeparator()
            + Utils.makeHyperlink("pastebin", "https://pastebin.com") + " " + "Free: 512KB - Paid users get: 10MB" + System.lineSeparator();

    /**
     *
     */
    public CmdPaste() {
        super();
        name = "paste";
        guildOnly = false;
        aliases = new String[]{"pastesites", "pastetools"};
        help = "A short list of all the paste tools out there to help share code.";
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final var embed = new EmbedBuilder();
        final var channel = event.getTextChannel();

        embed.setTitle("Paste tools");
        embed.setDescription(BODY);
        embed.setColor(Color.GREEN);
        embed.setTimestamp(Instant.now());

        channel.sendMessage(embed.build()).queue();
    }
}
