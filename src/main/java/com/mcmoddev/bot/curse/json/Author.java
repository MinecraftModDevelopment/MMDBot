package com.mcmoddev.bot.curse.json;

public class Author {
    
    public String Name;
    public String Url;
    
    public Author(String name, String url) {
        Name = name;
        Url = url;
    }
    
    public String getName() {
        return Name;
    }
    
    public String getUrl() {
        return Url;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Author{");
        sb.append("Name='").append(Name).append('\'');
        sb.append(", Url='").append(Url).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
