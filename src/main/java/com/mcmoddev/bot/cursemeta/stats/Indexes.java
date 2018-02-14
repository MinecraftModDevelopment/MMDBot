package com.mcmoddev.bot.cursemeta.stats;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Indexes {

    @SerializedName("ids")
    @Expose
    private final List<Long> ids = null;

    @SerializedName("modpacks")
    @Expose
    private final List<Long> modpacks = null;

    @SerializedName("mods")
    @Expose
    private final List<Long> mods = null;

    @SerializedName("timestamp_human")
    @Expose
    private String timestampHuman;

    @SerializedName("timestamp")
    @Expose
    private int timestamp;

    public List<Long> getIds () {

        return this.ids;
    }

    public List<Long> getModpacks () {

        return this.modpacks;
    }

    public List<Long> getMods () {

        return this.mods;
    }

    public String getTimestampHuman () {

        return this.timestampHuman;
    }

    public int getTimestamp () {

        return this.timestamp;
    }
}