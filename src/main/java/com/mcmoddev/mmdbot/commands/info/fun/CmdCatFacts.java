package com.mcmoddev.mmdbot.commands.info.fun;

import com.google.common.base.Charsets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

/**
 *
 * @author
 *
 */
public final class CmdCatFacts extends Command {

	/**
	 *
	 */
    public CmdCatFacts() {
        super();
        name = "catfacts";
        aliases = new String[]{"catfact", "cat-fact", "cat-facts"};
        help = "Get a random fact about cats, you learn something new every day!";
        guildOnly = false;
    }

    /**
     *
     * @return String.
     */
    public static String getFact() {
        try {
        	final URL url = new URL("https://catfact.ninja/fact");
        	final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            final BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
            final String inputLine = reader.readLine();
            reader.close();
            final JsonObject objectArray = JsonParser.parseString(inputLine).getAsJsonObject();
            return ":cat:  " + objectArray.get("fact").toString();

        } catch (final RuntimeException ex) {
            throw ex;
        } catch (final Exception ex) {
            MMDBot.LOGGER.error("Error getting cat fact...", ex);
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final EmbedBuilder embed = new EmbedBuilder();
        if (getFact() != null) {
            final Random random = new Random();
            embed.setColor((int) (random.nextInt(0x1000000)));
            embed.appendDescription(getFact());
            embed.setFooter("Puwerrd by https://catfact.ninja");

            event.getChannel().sendMessage(embed.build()).queue();
        }
    }
}
