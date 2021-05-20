package com.mcmoddev.mmdbot.tricks;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.commands.tricks.CmdRunTrick;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//TODO: Migrate to a SQLite DB with PR #45
public final class Tricks {
    private static final String TRICK_STORAGE_PATH = "mmdbot_tricks.json";

    private static @Nullable List<Trick> tricks = null;

    public static Optional<Trick> getTrick(String name) {
        return getTricks().stream().filter(trick -> trick.getNames().contains(name)).findAny();
    }

    public static List<CmdRunTrick> createTrickCommands() {
        return getTricks().stream().map(CmdRunTrick::new).collect(Collectors.toList());
    }

    public static List<Trick> getTricks() {
        if (tricks == null) {
            final File file = new File(TRICK_STORAGE_PATH);
            if (!file.exists())
                tricks = new ArrayList<>();
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type typeOfList = new TypeToken<List<Trick>>() {
                }.getType();
                tricks = new Gson().fromJson(reader, typeOfList);
            } catch (final IOException exception) {
                MMDBot.LOGGER.trace("Failed to read tricks file...", exception);
            }
            tricks = new ArrayList<>();
        }
        return tricks;
    }

    private static void write() {
        final File userJoinTimesFile = new File(TRICK_STORAGE_PATH);
        List<Trick> tricks = getTricks();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(userJoinTimesFile), StandardCharsets.UTF_8)) {
            new Gson().toJson(tricks, writer);
        } catch (final FileNotFoundException exception) {
            MMDBot.LOGGER.error("An FileNotFoundException occurred saving tricks...", exception);
        } catch (final IOException exception) {
            MMDBot.LOGGER.error("An IOException occurred saving tricks...", exception);
        }
    }

}
