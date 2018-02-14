package com.mcmoddev.bot.cursemeta;

import java.util.ArrayList;
import java.util.List;

public class ModInfo implements Comparable<ModInfo> {

    private static String PROJECT_PAGE_BASE = "https://minecraft.curseforge.com/projects/";

    private final long id;
    private String name;
    private long downloads;
    private double downloadsDaily; // Unreliable
    private double downloadsWeekly;
    private double downloadsMonthly;
    private final List<String> authors;

    public ModInfo (long id) {

        this.id = id;
        this.downloadsDaily = 0;
        this.downloadsWeekly = 0;
        this.downloadsMonthly = 0;
        this.authors = new ArrayList<>();
    }

    public String getName () {

        return this.name;
    }

    public void setName (String name) {

        this.name = name;
    }

    public long getDownloads () {

        return this.downloads;
    }

    public void setDownloads (long downloads) {

        this.downloads = downloads;
    }

    public double getDownloadsDaily () {

        return this.downloadsDaily;
    }

    public void setDownloadsDaily (double downloadsDaily) {

        this.downloadsDaily = downloadsDaily;
    }

    public double getDownloadsWeekly () {

        return this.downloadsWeekly;
    }

    public void setDownloadsWeekly (double downloadsWeekly) {

        this.downloadsWeekly = downloadsWeekly;
    }

    public double getDownloadsMonthly () {

        return this.downloadsMonthly;
    }

    public void setDownloadsMonthly (double downloadsMonthly) {

        this.downloadsMonthly = downloadsMonthly;
    }

    public long getId () {

        return this.id;
    }

    public void addAuthor (String name) {

        this.authors.add(name);
    }

    public List<String> getAuthors () {

        return this.authors;
    }

    public String getProjectPage () {

        return PROJECT_PAGE_BASE + this.id;
    }

    @Override
    public int compareTo (ModInfo o) {

        // This mod has more downloads.
        if (this.downloads > o.downloads) {

            return 1;
        }

        // Both mods have same download count.
        else if (this.downloads == o.downloads) {

            return 0;
        }

        // This mod has less downloads.
        return -1;
    }
}
