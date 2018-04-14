package com.mcmoddev.bot.curse;

import com.mcmoddev.bot.curse.CurseTracker;
import com.mcmoddev.bot.curse.json.Mod;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.util.EmbedBuilder;

import java.text.NumberFormat;
import java.util.*;

public class MessageMods extends EmbedBuilder {
    
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    
    private final List<Mod> sortedMods;
    private final int modsToShow;
    private final int hiddenMods;
    
    private long totalDownloads;
    private long totalMonthlyDownloads;
    private long hiddenDownloads;
    
    public MessageMods(int modsToShow, String... authors) {
        
        super();
        
        final String authorNames = Arrays.toString(authors);
        
        this.withTitle(authorNames.length() < 250 ? authorNames : authors.length + " authors");
        this.sortedMods = this.getModsForAuthors(authors);
        this.modsToShow = modsToShow;
        this.hiddenMods = Math.max(0, this.sortedMods.size() - modsToShow);
        
        this.setLenient(true);
        this.setDownloadInfo();
        
        final Random rand = new Random();
        this.withColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }
    
    private void setDownloadInfo() {
        
        if(this.sortedMods.isEmpty()) {
            
            this.withDesc("No mods found were found. Make sure usernames match those on CurseForge.");
            return;
        }
        
        int modCount = 0;
        final StringJoiner projectText = new StringJoiner(MessageUtils.SEPERATOR);
        double total = 0;
        for(final Mod mod : this.sortedMods) {
            total += mod.getPopularityScore();
            this.totalDownloads += mod.getDownloadCount();
            
            this.totalMonthlyDownloads += mod.getMonthly();
            
            if(modCount < this.modsToShow) {
                
                modCount++;
                projectText.add(MessageUtils.makeHyperlink(mod.getName(), mod.getWebSiteURL()) + " - " + NUMBER_FORMAT.format(mod.getDownloadCount()));
            } else {
                
                this.hiddenDownloads += mod.getDownloadCount();
            }
        }
        
        if(this.hiddenMods > 0) {
            
            projectText.add(String.format("Other Mods (%d) - %s", this.hiddenMods, NUMBER_FORMAT.format(this.hiddenDownloads)));
        }
        
        projectText.add(" ");
        projectText.add(MessageUtils.makeBold("Total Downloads: ") + NUMBER_FORMAT.format(this.totalDownloads));
        projectText.add("Total Mods: " + this.sortedMods.size());
        projectText.add("Monthly Downloads: " + NUMBER_FORMAT.format(this.totalMonthlyDownloads));
        projectText.add("Yearly Downloads (projected): " + NUMBER_FORMAT.format(this.totalMonthlyDownloads * 12));
        
        
        final float percentage = (float) this.totalDownloads / (float) CurseTracker.instance.getAllDownloads() * 100f;
        
        if(percentage >= 0.01) {
            
            projectText.add(String.format("Percentage of all downloads: %.2f", percentage) + "%");
        }
        
        this.withDesc(projectText.toString());
    }
    
    private List<Mod> getModsForAuthors(String... authors) {
        
        final Set<Mod> mods = new HashSet<>();
        
        for(final String author : authors) {
            
            mods.addAll(CurseTracker.instance.getModsForAuthor(author));
        }
        
        final List<Mod> sorted = new ArrayList<>(mods);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }
}