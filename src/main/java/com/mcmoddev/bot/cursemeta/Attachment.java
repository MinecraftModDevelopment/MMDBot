
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
