package com.mcmoddev.bot.cursemeta;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Index {

    @SerializedName("timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("timestamp_human")
    @Expose
    private String timestampHuman;
    @SerializedName("ids")
    @Expose
    private List<Long> ids = null;
    @SerializedName("mods")
    @Expose
    private List<Long> mods = null;
    @SerializedName("modpacks")
    @Expose
    private List<Long> modpacks = null;

    public Long getTimestamp () {

        return this.timestamp;
    }

    public void setTimestamp (Long timestamp) {

        this.timestamp = timestamp;
    }

    public String getTimestampHuman () {

        return this.timestampHuman;
    }

    public void setTimestampHuman (String timestampHuman) {

        this.timestampHuman = timestampHuman;
    }

    public List<Long> getIds () {

        return this.ids;
    }

    public void setIds (List<Long> ids) {

        this.ids = ids;
    }

    public List<Long> getMods () {

        return this.mods;
    }

    public void setMods (List<Long> mods) {

        this.mods = mods;
    }

    public List<Long> getModpacks () {

        return this.modpacks;
    }

    public void setModpacks (List<Long> modpacks) {

        this.modpacks = modpacks;
    }

}