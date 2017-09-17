package com.mcmoddev.bot.handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.cursemeta.Index;
import com.mcmoddev.bot.cursemeta.Project;
import com.mcmoddev.bot.cursemeta.discord.ProjectMessage;
import com.mcmoddev.bot.util.Utilities;

public class ScheduleHandler {

    private static final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
    private static final Gson gson = new GsonBuilder().create();

    public ScheduleHandler () {

        // Downloads file if it doesn't exist.
        if (!new File("downloads/index.json").exists()) {

            Utilities.saveFileMMD("https://cursemeta.dries007.net/index.json", "index.json");
        }

        ses.scheduleAtFixedRate(ScheduleHandler::updateNewMods, 1, 1, TimeUnit.HOURS);
    }

    public static void updateNewMods () {

        try {

            final Index oldIndex = gson.fromJson(new JsonReader(new FileReader(new File("downloads/index.json"))), Index.class);
            final Index newIndex = gson.fromJson(new JsonReader(new FileReader(Utilities.saveFileMMD("https://cursemeta.dries007.net/index.json", "index.json"))), Index.class);

            newIndex.getMods().removeAll(oldIndex.getMods());

            for (final Long integer : newIndex.getMods()) {

                final Project project = gson.fromJson(new JsonReader(new FileReader(Utilities.saveFileMMD("https://cursemeta.dries007.net/" + integer + ".json", integer + ".json"))), Project.class);

                Utilities.sendMessage(MMDBot.state.getCurseChannel(), new ProjectMessage(project).build());
            }

            if (newIndex.getMods().size() < 1) {
                Utilities.sendMessage(MMDBot.state.getCurseChannel(), "There were no new mods since the last check.");
            }
        }

        catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
