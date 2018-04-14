package com.mcmoddev.bot.curse.json;

public class Mod implements Comparable<Mod> {
    
    public Author[] Authors;
    public Category[] Categories;
    public double DownloadCount;
    public int GamePopularityRank;
    public int Id;
    public String Name;
    public float PopularityScore;
    public String PrimaryAuthorName;
    public String WebSiteURL;
    public String PackageType;
    public int monthly;
    
    public Mod(Author[] authors, Category[] categories, double downloadCount, int gamePopularityRank, int id, String name, float popularityScore, String primaryAuthorName, String webSiteURL, String packageType, int monthly) {
        Authors = authors;
        Categories = categories;
        DownloadCount = downloadCount;
        GamePopularityRank = gamePopularityRank;
        Id = id;
        Name = name;
        PopularityScore = popularityScore;
        PrimaryAuthorName = primaryAuthorName;
        WebSiteURL = webSiteURL;
        PackageType = packageType;
        this.monthly = monthly;
    }
    
    
    public Author[] getAuthors() {
        return Authors;
    }
    
    public void setAuthors(Author[] authors) {
        Authors = authors;
    }
    
    public Category[] getCategories() {
        return Categories;
    }
    
    public void setCategories(Category[] categories) {
        Categories = categories;
    }
    
    public double getDownloadCount() {
        return DownloadCount;
    }
    
    public void setDownloadCount(double downloadCount) {
        DownloadCount = downloadCount;
    }
    
    public int getGamePopularityRank() {
        return GamePopularityRank;
    }
    
    public void setGamePopularityRank(int gamePopularityRank) {
        GamePopularityRank = gamePopularityRank;
    }
    
    public int getId() {
        return Id;
    }
    
    public void setId(int id) {
        Id = id;
    }
    
    public String getName() {
        return Name;
    }
    
    public void setName(String name) {
        Name = name;
    }
    
    public float getPopularityScore() {
        return PopularityScore;
    }
    
    public void setPopularityScore(float popularityScore) {
        PopularityScore = popularityScore;
    }
    
    public String getPrimaryAuthorName() {
        return PrimaryAuthorName;
    }
    
    public void setPrimaryAuthorName(String primaryAuthorName) {
        PrimaryAuthorName = primaryAuthorName;
    }
    
    public String getWebSiteURL() {
        return WebSiteURL;
    }
    
    public void setWebSiteURL(String webSiteURL) {
        WebSiteURL = webSiteURL;
    }
    
    public String getPackageType() {
        return PackageType;
    }
    
    public void setPackageType(String packageType) {
        PackageType = packageType;
    }
    
    public int getMonthly() {
        return monthly;
    }
    
    public void setMonthly(int monthly) {
        this.monthly = monthly;
    }
    
    @Override
    public int compareTo(Mod o) {
        
        // This mod has more downloads.
        if(this.getDownloadCount() > o.getDownloadCount()) {
            
            return 1;
        }
        
        // Both mods have same download count.
        else if(this.getDownloadCount() == o.getDownloadCount()) {
            
            return 0;
        }
        
        // This mod has less downloads.
        return -1;
    }
}
