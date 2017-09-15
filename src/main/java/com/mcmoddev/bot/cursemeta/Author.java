
package com.mcmoddev.bot.cursemeta;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mcmoddev.bot.util.Utilities;

public class Author {

    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("Url")
    @Expose
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getHyperlink() {
        
        return Utilities.makeHyperlink(this.name, this.url.replace(".aspx", ""));
    }
}