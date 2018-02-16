package com.mcmoddev.bot.cursemeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.util.EmbedBuilder;

public class MessageMods extends EmbedBuilder {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private final List<ModInfo> sortedMods;
    private final int modsToShow;
    private final int hiddenMods;

    private long totalDownloads;
    private long totalMonthlyDownloads;
    private long hiddenDownloads;

    public MessageMods (int modsToShow, String... authors) {

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

    private void setDownloadInfo () {

        if (this.sortedMods.isEmpty()) {

            this.withDesc("No mods found were found. Make sure usernames match those on CurseForge.");
            return;
        }

        int modCount = 0;
        final StringJoiner projectText = new StringJoiner(MessageUtils.SEPERATOR);

        for (final ModInfo mod : this.sortedMods) {

            this.totalDownloads += mod.getDownloads();
            this.totalMonthlyDownloads += mod.getDownloadsMonthly();
            
            if (modCount < this.modsToShow) {

                modCount++;
                projectText.add(MessageUtils.makeHyperlink(mod.getName(), mod.getProjectPage()) + " - " + NUMBER_FORMAT.format(mod.getDownloads()));
            }

            else {

                this.hiddenDownloads += mod.getDownloads();
            }
        }

        if (this.hiddenMods > 0) {

            projectText.add(String.format("Other Mods (%d) - %s", this.hiddenMods, NUMBER_FORMAT.format(this.hiddenDownloads)));
        }

        projectText.add(" ");
        projectText.add(MessageUtils.makeBold("Total Downloads: ") + NUMBER_FORMAT.format(this.totalDownloads));
        projectText.add("Total Mods: " + this.sortedMods.size());
        projectText.add("Monthly Downloads: " + NUMBER_FORMAT.format(this.totalMonthlyDownloads));

        final float percentage = (float) this.totalDownloads / (float) CurseMetaTracker.instance.getAllDownloads() * 100f;

        if (percentage >= 0.01) {

            projectText.add(String.format("Percentage of all downloads: %.2f", percentage) + "%");
        }

        this.withDesc(projectText.toString());
    }

    private List<ModInfo> getModsForAuthors (String... authors) {

        final Set<ModInfo> mods = new HashSet<>();

        for (final String author : authors) {

            mods.addAll(CurseMetaTracker.instance.getAuthors().get(author.toLowerCase()));
        }

        final List<ModInfo> sorted = new ArrayList<>(mods);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }
}