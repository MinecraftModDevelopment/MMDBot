package com.mcmoddev.bot.curse;

import com.google.gson.*;
import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.curse.json.*;
import sx.blah.discord.handle.obj.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.*;

public class CurseTracker {
    
    private static final Gson GSON = new GsonBuilder().create();
    public static final File CURSE_DIR = new File(MMDBot.DATA_DIR, "curse");
    public static CurseTracker instance;
    private final MMDBot bot;
    
    public static final String URL_INDEX = "https://curse.nikky.moe/api/addon";
    public static final String URL_API = "https://cursemeta.dries007.net/api/v2/direct/v2GetAddOns";
    public static final String URL_MONTHLY = "https://staging_cursemeta.dries007.net/api/v2/history/downloads/432/monthly";
    
    public static List<IndexFile> indices = new ArrayList<>();
    public static List<Mod> mods = new ArrayList<>();
    
    private long allDownloads;
    
    public CurseTracker(MMDBot bot) {
        
        this.bot = bot;
        this.bot.timer.scheduleAndRunHourly(1, this::updateCurseData);
        instance = this;
    }
    
    public void updateCurseData() {
        if(!CURSE_DIR.exists()) {
            CURSE_DIR.mkdirs();
        }
        
        this.bot.getClient().changePresence(StatusType.DND, ActivityType.WATCHING, "The Database Update");
        File indexFile = bot.downloadFile(URL_INDEX, CURSE_DIR, "index.json");
        File monthlyFile = bot.downloadFile(URL_MONTHLY, CURSE_DIR, "monthly.json");
        
        try(FileReader reader = new FileReader(indexFile)) {
            indices = new ArrayList<>(Arrays.asList(GSON.fromJson(reader, IndexFile[].class)));
        } catch(IOException e) {
            e.printStackTrace();
        }
        Map<String, Double> monthly = new HashMap<>();
        try(FileReader reader = new FileReader(monthlyFile)) {
            monthly = GSON.fromJson(reader, Map.class);
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        indices.sort(Comparator.comparingInt(IndexFile::getId));
        
        StringBuilder builder = new StringBuilder("{\"ids\": [");
        for(IndexFile index : indices) {
            builder.append("\"").append(index.getId()).append("\"").append(",");
        }
        builder.reverse().deleteCharAt(0).reverse();
        
        builder.append("]}");
        String data = sendPost(builder.toString());
        if(!data.isEmpty()) {
            mods = new ArrayList<>(Arrays.asList(GSON.fromJson(data, Mod[].class)));
        }
        mods.sort(Comparator.comparingDouble(Mod::getDownloadCount));
        allDownloads = 0;
        for(Mod mod : mods) {
            allDownloads += mod.getDownloadCount();
        }
        for(Map.Entry<String, Double> entry : monthly.entrySet()) {
            int id = Integer.parseInt(entry.getKey());
            if(getModFromID(id) != null)
                getModFromID(id).setMonthly(entry.getValue().intValue());
        }
        System.out.println("ready!");
        this.bot.getClient().changePresence(StatusType.ONLINE, ActivityType.LISTENING, "for any commands");
    }
    
    
    private static String sendPost(String args) {
        try {
            URL obj = new URL(URL_API);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            
            con.setRequestMethod("POST");
            con.setRequestProperty("content-type", "application/json");
            
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(args);
            wr.flush();
            wr.close();
            
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + URL_API);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch(IOException e) {
            e.printStackTrace();
        }
        
        return "";
    }
    
    public List<IndexFile> getIndicesForAuthor(String auth) {
        List<IndexFile> files = new ArrayList<>();
        for(IndexFile index : indices) {
            for(Author author : index.getAuthors()) {
                if(author.getName().equalsIgnoreCase(auth)) {
                    files.add(index);
                }
            }
        }
        return files;
    }
    
    
    public List<Mod> getModsForAuthor(String auth) {
        List<Mod> modList = new ArrayList<>();
        for(Mod mod : mods) {
            for(Author author : mod.getAuthors()) {
                if(author.getName().equalsIgnoreCase(auth)) {
                    modList.add(mod);
                }
            }
        }
        
        return modList;
    }
    
    public Mod getModFromName(String name) {
        for(Mod mod : mods) {
            if(!mod.getPackageType().equalsIgnoreCase("mod")) {
                continue;
            }
            if(mod.getName().equalsIgnoreCase(name)) {
                return mod;
            }
        }
        return null;
    }
    
    
    public Mod getModFromID(int ID) {
        for(Mod mod : mods) {
            if(mod.getId() == ID) {
                return mod;
            }
        }
        return null;
    }
    
    public long getAllDownloads() {
        return allDownloads;
    }
    
    public List<Mod> getMods() {
        return mods;
    }
}
