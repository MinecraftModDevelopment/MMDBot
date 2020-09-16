package com.mcmoddev.mmdbot.commands.fun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class CmdCatFacts extends Command {

    public CmdCatFacts() {
        super();
        name = "catfacts";
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
        if (getFact() != null) {
            event.getChannel().sendMessage(getFact()).queue();
        }
    }
}
