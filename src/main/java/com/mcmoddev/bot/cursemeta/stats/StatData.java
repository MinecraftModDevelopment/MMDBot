package com.mcmoddev.bot.cursemeta.stats;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatData {

    @SerializedName("timestamp_human")
    @Expose
    private String timestampHuman;

    @SerializedName("timestamp")
    @Expose
    private int timestamp;

    @SerializedName("stats")
    @Expose
    private Stats stats;

    public String getTimestampHuman () {

        return this.timestampHuman;
    }

    public int getTimestamp () {

        return this.timestamp;
    }

    public Stats getStats () {

        return this.stats;
    }
}
