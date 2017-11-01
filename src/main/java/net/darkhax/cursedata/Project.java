package net.darkhax.cursedata;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Project {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("game")
    @Expose
    private String game;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("thumbnail")
    @Expose
    private String thumbnail;
    @SerializedName("authors")
    @Expose
    private List<String> authors = null;
    @SerializedName("downloads")
    @Expose
    private Downloads downloads;
    @SerializedName("favorites")
    @Expose
    private long favorites;
    @SerializedName("likes")
    @Expose
    private long likes;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("project_url")
    @Expose
    private String projectUrl;
    @SerializedName("release_type")
    @Expose
    private String releaseType;
    @SerializedName("license")
    @Expose
    private String license;

    public String getTitle () {

        return this.title;
    }

    public void setTitle (String title) {

        this.title = title;
    }

    public Project withTitle (String title) {

        this.title = title;
        return this;
    }

    public String getGame () {

        return this.game;
    }

    public void setGame (String game) {

        this.game = game;
    }

    public Project withGame (String game) {

        this.game = game;
        return this;
    }

    public String getCategory () {

        return this.category;
    }

    public void setCategory (String category) {

        this.category = category;
    }

    public Project withCategory (String category) {

        this.category = category;
        return this;
    }

    public String getUrl () {

        return this.url;
    }

    public void setUrl (String url) {

        this.url = url;
    }

    public Project withUrl (String url) {

        this.url = url;
        return this;
    }

    public String getThumbnail () {

        return this.thumbnail;
    }

    public void setThumbnail (String thumbnail) {

        this.thumbnail = thumbnail;
    }

    public Project withThumbnail (String thumbnail) {

        this.thumbnail = thumbnail;
        return this;
    }

    public List<String> getAuthors () {

        return this.authors;
    }

    public void setAuthors (List<String> authors) {

        this.authors = authors;
    }

    public Project withAuthors (List<String> authors) {

        this.authors = authors;
        return this;
    }

    public Downloads getDownloads () {

        return this.downloads;
    }

    public long getMonthlyDownloads () {

        return this.downloads.getMonthly();
    }

    public long getTotalDownloads () {

        return this.downloads.getTotal();
    }

    public void setDownloads (Downloads downloads) {

        this.downloads = downloads;
    }

    public Project withDownloads (Downloads downloads) {

        this.downloads = downloads;
        return this;
    }

    public long getFavorites () {

        return this.favorites;
    }

    public void setFavorites (long favorites) {

        this.favorites = favorites;
    }

    public Project withFavorites (long favorites) {

        this.favorites = favorites;
        return this;
    }

    public long getLikes () {

        return this.likes;
    }

    public void setLikes (long likes) {

        this.likes = likes;
    }

    public Project withLikes (long likes) {

        this.likes = likes;
        return this;
    }

    public String getUpdatedAt () {

        return this.updatedAt;
    }

    public void setUpdatedAt (String updatedAt) {

        this.updatedAt = updatedAt;
    }

    public Project withUpdatedAt (String updatedAt) {

        this.updatedAt = updatedAt;
        return this;
    }

    public String getCreatedAt () {

        return this.createdAt;
    }

    public void setCreatedAt (String createdAt) {

        this.createdAt = createdAt;
    }

    public Project withCreatedAt (String createdAt) {

        this.createdAt = createdAt;
        return this;
    }

    public String getProjectUrl () {

        return "https:" + this.projectUrl;
    }

    public void setProjectUrl (String projectUrl) {

        this.projectUrl = projectUrl;
    }

    public Project withProjectUrl (String projectUrl) {

        this.projectUrl = projectUrl;
        return this;
    }

    public String getReleaseType () {

        return this.releaseType;
    }

    public void setReleaseType (String releaseType) {

        this.releaseType = releaseType;
    }

    public Project withReleaseType (String releaseType) {

        this.releaseType = releaseType;
        return this;
    }

    public String getLicense () {

        return this.license;
    }

    public void setLicense (String license) {

        this.license = license;
    }

    public Project withLicense (String license) {

        this.license = license;
        return this;
    }

    @Override
    public String toString () {

        final String N = System.getProperty("line.separator");

        return "Title - " + this.title + N + "Game - " + this.game + N + "Category - " + this.category + N + "URL - " + this.url + N + "Thumbnail - " + this.thumbnail + N + "Authors - " + this.authors.toString() + N + "Monthly DL - " + this.getMonthlyDownloads() + N + "Total DL - " + this.getTotalDownloads() + N + "Favourites - " + this.favorites + N + "Likes - " + this.likes + N + "Last Updated - " + this.updatedAt + N + "Created - " + this.createdAt + N + "Project URL - " + this.getProjectUrl() + N + "releaseType - " + this.releaseType + N + "License - " + this.license;
    }
}
