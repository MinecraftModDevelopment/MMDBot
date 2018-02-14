package com.mcmoddev.bot.cursemeta.stats;

import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stats {

    @SerializedName("projects")
    @Expose
    private Map<Long, Projects> projects;

    @SerializedName("authors")
    @Expose
    private Map<String, Authors> authors;

    public Map<Long, Projects> getProjects () {

        return this.projects;
    }

    public Map<String, Authors> getAuthors () {

        return this.authors;
    }
}
