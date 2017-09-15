
package com.mcmoddev.bot.cursemeta;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attachment {

    @SerializedName("Description")
    @Expose
    private String description;
    @SerializedName("IsDefault")
    @Expose
    private Boolean isDefault;
    @SerializedName("ThumbnailUrl")
    @Expose
    private String thumbnailUrl;
    @SerializedName("Title")
    @Expose
    private String title;
    @SerializedName("Url")
    @Expose
    private String url;

    public String getDescription () {

        return this.description;
    }

    public void setDescription (String description) {

        this.description = description;
    }

    public Boolean getIsDefault () {

        return this.isDefault;
    }

    public void setIsDefault (Boolean isDefault) {

        this.isDefault = isDefault;
    }

    public String getThumbnailUrl () {

        return this.thumbnailUrl;
    }

    public void setThumbnailUrl (String thumbnailUrl) {

        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle () {

        return this.title;
    }

    public void setTitle (String title) {

        this.title = title;
    }

    public String getUrl () {

        return this.url;
    }

    public void setUrl (String url) {

        this.url = url;
    }

}
