package com.mcmoddev.bot.cursemeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
        this.sortedMods = this.getModsForAuthors(authors);
        this.modsToShow = modsToShow;
        this.hiddenMods = Math.max(0, this.sortedMods.size() - modsToShow);

        this.setLenient(true);
        this.setDownloadInfo();
    }

    private void setDownloadInfo () {

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

        projectText.add(String.format("Other Mods (%d) - %s", this.hiddenMods, NUMBER_FORMAT.format(this.hiddenDownloads)));
        projectText.add(" ");
        projectText.add(MessageUtils.makeBold("Total Downloads: ") + NUMBER_FORMAT.format(this.totalDownloads));
        projectText.add("Total Mods: " + this.sortedMods.size());
        projectText.add("Monthly Downloads: " + NUMBER_FORMAT.format(this.totalMonthlyDownloads));

        this.withDesc(projectText.toString());
    }

    private List<ModInfo> getModsForAuthors (String... authors) {

        final Set<ModInfo> mods = new HashSet<>();

        for (final String author : authors) {

            mods.addAll(CurseMetaTracker.instance.authors.get(author));
        }

        final List<ModInfo> sorted = new ArrayList<>(mods);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }
}