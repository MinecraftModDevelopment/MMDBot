package com.mcmoddev.bot.curse.json;

public class Category {
    
    public int Id;
    public String Name;
    public String URL;
    
    public Category(int id, String name, String URL) {
        Id = id;
        Name = name;
        this.URL = URL;
    }
    
    public int getId() {
        return Id;
    }
    
    public String getName() {
        return Name;
    }
    
    public String getURL() {
        return URL;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Category{");
        sb.append("Id=").append(Id);
        sb.append(", Name='").append(Name).append('\'');
        sb.append(", URL='").append(URL).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
