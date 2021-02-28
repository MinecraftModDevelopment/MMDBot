package com.mcmoddev.mmdbot.commands.info.fun;

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

public class CmdCatFacts extends Command {

    public CmdCatFacts() {
        super();
        name = "catfacts";
        aliases = new String[]{"catfact", "cat-fact", "cat-facts"};
        help = "Get a random fact about cats, you learn something new every day!";
        guildOnly = false;
    }

    public static String getFact() {
        try {
            URL url = new URL("https://catfact.ninja/fact");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
            String inputLine = reader.readLine();
            JsonObject objectArray = JsonParser.parseString(inputLine).getAsJsonObject();
            return ":cat:  " + objectArray.get("fact").toString();

        } catch (Exception exception) {
            MMDBot.LOGGER.error("Error getting cat fact...", exception);
            exception.printStackTrace();
        }
        return null;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (!Utils.checkCommand(this, event)) return;
        EmbedBuilder embed = new EmbedBuilder();
        if (getFact() != null) {
            embed.setColor((int) (Math.random() * 0x1000000));
            embed.appendDescription(getFact());
            embed.setFooter("Puwerrd by https://catfact.ninja");

            event.getChannel().sendMessage(embed.build()).queue();
        }
    }
}
