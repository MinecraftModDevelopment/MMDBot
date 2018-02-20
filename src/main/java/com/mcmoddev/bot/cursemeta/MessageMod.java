package com.mcmoddev.bot.cursemeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;

import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.util.EmbedBuilder;

public class MessageMod extends EmbedBuilder {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    private final List<ModInfo> sortedMods;
    private final int modsToShow;
    private final int hiddenMods;

    private long totalDownloads;
    private long totalMonthlyDownloads;
    private long hiddenDownloads;

    public MessageMod (int modsToShow, String... mods) {

        super();

        final String modNames = Arrays.toString(mods);

        this.withTitle(modNames.length() < 250 ? modNames : mods.length + " mods");
        this.sortedMods = this.getMods(mods);
        this.modsToShow = modsToShow;
        this.hiddenMods = Math.max(0, this.sortedMods.size() - modsToShow);

        this.setLenient(true);
        this.setDownloadInfo();

        final Random rand = new Random();
        this.withColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
    }

    private void setDownloadInfo () {

        if (this.sortedMods.isEmpty()) {

            this.withDesc("No mods were found. Make sure mod names match those on CurseForge.");
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

    private List<ModInfo> getMods (String... modNames) {

        final Set<ModInfo> mods = new HashSet<>();
        final List<String> modList = Arrays.asList(modNames);
        for (final Map.Entry<Long, ModInfo> entry : CurseMetaTracker.instance.getMods().entrySet()) {
            if (modList.contains(entry.getValue().getName().toLowerCase().replaceAll(" ", "-"))) {
                mods.add(entry.getValue());
            }
            else if (modList.contains(entry.getKey().toString())) {
                mods.add(entry.getValue());
            }
        }

        final List<ModInfo> sorted = new ArrayList<>(mods);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted;
    }
}