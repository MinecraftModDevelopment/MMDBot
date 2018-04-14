package com.mcmoddev.bot.curse.json;

import java.util.Arrays;

public class IndexFile {
    public int id;
    public Author[] authors;
    
    public int getId() {
        return id;
    }
    
    public Author[] getAuthors() {
        return authors;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public void setAuthors(Author[] authors) {
        this.authors = authors;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IndexFile{");
        sb.append("id=").append(id);
        sb.append(", authors=").append(Arrays.toString(authors));
        sb.append('}');
        return sb.toString();
    }
}
