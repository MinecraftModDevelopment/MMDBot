
package com.mcmoddev.bot.cursemeta;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Project {

    @SerializedName("Attachments")
    @Expose
    private List<Attachment> attachments = null;
    @SerializedName("Authors")
    @Expose
    private List<Author> authors = null;
    @SerializedName("Categories")
    @Expose
    private List<Category> categories = null;
    @SerializedName("CategorySection")
    @Expose
    private CategorySection categorySection;
    @SerializedName("CommentCount")
    @Expose
    private Long commentCount;
    @SerializedName("DefaultFileId")
    @Expose
    private Long defaultFileId;
    @SerializedName("DownloadCount")
    @Expose
    private Double downloadCount;
    @SerializedName("GameId")
    @Expose
    private Long gameId;
    @SerializedName("GamePopularityRank")
    @Expose
    private Long gamePopularityRank;
    @SerializedName("GameVersionLatestFiles")
    @Expose
    private List<GameVersionLatestFile> gameVersionLatestFiles = null;
    @SerializedName("IconId")
    @Expose
    private Long iconId;
    @SerializedName("Id")
    @Expose
    private Long id;
    @SerializedName("InstallCount")
    @Expose
    private Long installCount;
    @SerializedName("IsFeatured")
    @Expose
    private Long isFeatured;
    @SerializedName("LatestFiles")
    @Expose
    private List<LatestFile> latestFiles = null;
    @SerializedName("Likes")
    @Expose
    private Long likes;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PackageType")
    @Expose
    private String packageType;
    @SerializedName("PopularityScore")
    @Expose
    private Double popularityScore;
    @SerializedName("PrimaryAuthorName")
    @Expose
    private String primaryAuthorName;
    @SerializedName("PrimaryCategoryId")
    @Expose
    private Long primaryCategoryId;
    @SerializedName("Rating")
    @Expose
    private Long rating;
    @SerializedName("Stage")
    @Expose
    private String stage;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("Summary")
    @Expose
    private String summary;
    @SerializedName("WebSiteURL")
    @Expose
    private String webSiteURL;

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public CategorySection getCategorySection() {
        return categorySection;
    }

    public void setCategorySection(CategorySection categorySection) {
        this.categorySection = categorySection;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

    public Long getDefaultFileId() {
        return defaultFileId;
    }

    public void setDefaultFileId(Long defaultFileId) {
        this.defaultFileId = defaultFileId;
    }

    public Double getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Double downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getGamePopularityRank() {
        return gamePopularityRank;
    }

    public void setGamePopularityRank(Long gamePopularityRank) {
        this.gamePopularityRank = gamePopularityRank;
    }

    public List<GameVersionLatestFile> getGameVersionLatestFiles() {
        return gameVersionLatestFiles;
    }

    public void setGameVersionLatestFiles(List<GameVersionLatestFile> gameVersionLatestFiles) {
        this.gameVersionLatestFiles = gameVersionLatestFiles;
    }

    public Long getIconId() {
        return iconId;
    }

    public void setIconId(Long iconId) {
        this.iconId = iconId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstallCount() {
        return installCount;
    }

    public void setInstallCount(Long installCount) {
        this.installCount = installCount;
    }

    public Long getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Long isFeatured) {
        this.isFeatured = isFeatured;
    }

    public List<LatestFile> getLatestFiles() {
        return latestFiles;
    }

    public void setLatestFiles(List<LatestFile> latestFiles) {
        this.latestFiles = latestFiles;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public Double getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(Double popularityScore) {
        this.popularityScore = popularityScore;
    }

    public String getPrimaryAuthorName() {
        return primaryAuthorName;
    }

    public void setPrimaryAuthorName(String primaryAuthorName) {
        this.primaryAuthorName = primaryAuthorName;
    }

    public Long getPrimaryCategoryId() {
        return primaryCategoryId;
    }

    public void setPrimaryCategoryId(Long primaryCategoryId) {
        this.primaryCategoryId = primaryCategoryId;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getWebSiteURL() {
        return webSiteURL;
    }

    public void setWebSiteURL(String webSiteURL) {
        this.webSiteURL = webSiteURL;
    }

}
