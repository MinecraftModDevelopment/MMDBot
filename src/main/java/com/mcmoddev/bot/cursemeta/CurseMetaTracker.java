package com.mcmoddev.bot.cursemeta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.cursemeta.stats.Authors;
import com.mcmoddev.bot.cursemeta.stats.Indexes;
import com.mcmoddev.bot.cursemeta.stats.Projects;
import com.mcmoddev.bot.cursemeta.stats.StatData;

public class CurseMetaTracker {

    private static final Gson GSON = new GsonBuilder().create();

    public static final String DAILY = "https://cursemeta.dries007.net/daily.json";
    public static final String WEEKLY = "https://cursemeta.dries007.net/weekly.json";
    public static final String MONTHLY = "https://cursemeta.dries007.net/monthly.json";
    public static final String STATS = "https://cursemeta.dries007.net/stats.json";
    public static final String INDEX = "https://cursemeta.dries007.net/index.json";

    // TODO replace with a getter method that auto makes folder if it doesn't exist.
    public static final File CURSE_DIR = new File(MMDBot.DATA_DIR, "curse");

    public static CurseMetaTracker instance;
    private final MMDBot bot;
    private List<Long> knownModIds;
    private long allDownloads;
    private List<Long> newModIds;
    private Map<Long, ModInfo> mods;
    private ArrayListMultimap<String, ModInfo> authors;

    public CurseMetaTracker (MMDBot bot) {

        this.bot = bot;
        this.bot.timer.scheduleAndRunHourly(1, () -> this.updateCurseData());
        instance = this;
    }

    public void updateCurseData () {

        // Make the curse dir if it doesn't exist.
        if (!CURSE_DIR.exists()) {

            CURSE_DIR.mkdirs();
        }

        this.bot.getClient().dnd("Updating Database");

        // Download all of the needed files
        // TODO make this a bit more cleaner.
        this.bot.downloadFile(DAILY, "data/curse/daily.json");
        this.bot.downloadFile(WEEKLY, "data/curse/weekly.json");
        this.bot.downloadFile(MONTHLY, "data/curse/monthly.json");
        this.bot.downloadFile(STATS, "data/curse/stats.json");
        this.bot.downloadFile(INDEX, "data/curse/index.json");

        try {

            // Load all of the data into ram
            final StatData statData = GSON.fromJson(new FileReader("data/curse/stats.json"), StatData.class);
            final Indexes indexes = GSON.fromJson(new FileReader("data/curse/index.json"), Indexes.class);
            final LinkedTreeMap<String, Double> daily = (LinkedTreeMap) GSON.fromJson(new FileReader("data/curse/daily.json"), HashMap.class).get("delta");
            final LinkedTreeMap<String, Double> weekly = (LinkedTreeMap) GSON.fromJson(new FileReader("data/curse/weekly.json"), HashMap.class).get("delta");
            final LinkedTreeMap<String, Double> monthly = (LinkedTreeMap) GSON.fromJson(new FileReader("data/curse/monthly.json"), HashMap.class).get("delta");

            // Process the data into a useful format

            // If this is the first run, data is preInitialized.
            if (this.knownModIds == null) {

                this.knownModIds = indexes.getMods();
                this.newModIds = new ArrayList<>();
            }

            // If this isn't the first run, look for new mod ids before setting the known id
            // list.
            else {

                this.newModIds = new ArrayList<>(indexes.getMods());
                this.newModIds.removeAll(this.knownModIds);
                this.knownModIds = indexes.getMods();
            }

            // Mod list is overwritten to completely reset it.
            this.mods = new HashMap<>();
            this.allDownloads = 0;

            // Loop through all known mod ids and aggregate the data.
            for (final long id : this.knownModIds) {

                final ModInfo modInf = new ModInfo(id);
                final String projectId = Long.toString(id);

                // Sets the periodic downloads
                modInf.setDownloadsDaily(daily.getOrDefault(projectId, 0d));
                modInf.setDownloadsWeekly(weekly.getOrDefault(projectId, 0d));
                modInf.setDownloadsMonthly(monthly.getOrDefault(projectId, 0d));

                // Attempt to set stat data for the project.
                final Projects project = statData.getStats().getProjects().get(id);

                if (project != null) {

                    modInf.setDownloads(project.downloads);
                    this.allDownloads += project.downloads;
                    modInf.setName(project.name);
                    modInf.setPopularity(project.score);
                }

                // Place the aggregate data into the map.
                this.mods.put(id, modInf);
            }

            // Author list is overwritten to completely reset it.
            this.authors = ArrayListMultimap.create();

            // Go through all the authors
            for (final Entry<String, Authors> author : statData.getStats().getAuthors().entrySet()) {

                if (author.getValue().member == null || !author.getValue().member.containsKey("Mods")) {

                    continue;
                }

                // Find out what mods they're listed for
                for (final long projectId : author.getValue().member.get("Mods")) {

                    final ModInfo modInfo = this.mods.get(projectId);

                    if (modInfo != null) {

                        // Track the author and the projects they own.
                        final String authorName = author.getKey().toLowerCase();
                        modInfo.addAuthor(authorName);
                        this.authors.put(authorName, modInfo);
                    }
                }
            }
        }

        catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {

            e.printStackTrace();
        }

        this.bot.getClient().online("");
    }

    public List<Long> getKnownModIds () {

        return this.knownModIds;
    }

    public long getAllDownloads () {

        return this.allDownloads;
    }

    public List<Long> getNewModIds () {

        return this.newModIds;
    }

    public Map<Long, ModInfo> getMods () {

        return this.mods;
    }

    public ArrayListMultimap<String, ModInfo> getAuthors () {

        return this.authors;
    }
}