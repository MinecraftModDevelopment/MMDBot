
package com.mcmoddev.bot.cursemeta;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("Id")
    @Expose
    private Long id;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("URL")
    @Expose
    private String uRL;

    public Long getId () {

        return this.id;
    }

    public void setId (Long id) {

        this.id = id;
    }

    public String getName () {

        return this.name;
    }

    public void setName (String name) {

        this.name = name;
    }

    public String getURL () {

        return this.uRL;
    }

    public void setURL (String uRL) {

        this.uRL = uRL;
    }

}
