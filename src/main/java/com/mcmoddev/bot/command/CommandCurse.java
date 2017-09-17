package com.mcmoddev.bot.command;

import java.text.NumberFormat;

import com.mcmoddev.bot.util.Utilities;

import net.darkhax.cursedata.CurseData;
import net.darkhax.cursedata.Member;
import net.darkhax.cursedata.Project;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CommandCurse implements Command {

    private static NumberFormat nFormat = NumberFormat.getInstance();
    private long totalCurseDownloads;
    private long lastCheckTime;

    @Override
    public void processCommand (IMessage message, String[] params) {

        Utilities.sendMessage(message.getChannel(), "Getting curse data for " + params[1]);
        if (System.currentTimeMillis() - this.lastCheckTime >= 1000 * 3600 || this.lastCheckTime == 0 || this.totalCurseDownloads == 0) {
            this.lastCheckTime = System.currentTimeMillis();
            this.totalCurseDownloads = CurseData.getTotalCurseDownloads();
        }
        final StringBuilder builder = new StringBuilder();
        final EmbedBuilder embed = new EmbedBuilder();
        final Member member = CurseData.getMember(params[1]);

        if (member.getUsername().equals("%INVALID%") || member.getUsername().isEmpty()) {

            Utilities.sendMessage(message.getChannel(), "No user could be found by the name " + params[1]);
            return;
        }

        else if (member.getProjects().size() == 0) {

            Utilities.sendMessage(message.getChannel(), "No projects found for " + params[1]);
            return;
        }

        int addedProjects = 0;
        long otherDLs = 0;
        long total = 0;
        long monthly = 0;

        for (final Project project : member.getProjects()) {

            if (addedProjects < 10) {
                builder.append(Utilities.makeHyperlink(Utilities.sanatizeMarkdown(project.getTitle()), project.getProjectUrl()) + " - " + nFormat.format(project.getTotalDownloads()) + Utilities.SEPERATOR);
            }
            else {
                otherDLs += project.getTotalDownloads();
            }

            total += project.getTotalDownloads();
            monthly += project.getMonthlyDownloads();
            addedProjects++;
        }

        if (addedProjects >= 10) {
            builder.append("Other Projects (" + (member.getProjects().size() - 10) + ") - " + nFormat.format(otherDLs) + Utilities.SEPERATOR);
        }

        builder.append("Total Projects: " + member.getProjects().size() + Utilities.SEPERATOR);
        builder.append("Monthly Downloads: " + nFormat.format(monthly) + Utilities.SEPERATOR);

        embed.setLenient(true);
        embed.withDesc(builder.toString());
        embed.withColor((int) (Math.random() * 0x1000000));
        embed.withTitle("Total Downloads: " + nFormat.format(total) + " " + Utilities.getPercent(total, this.totalCurseDownloads));
        embed.withThumbnail(member.getAvatar());
        Utilities.sendMessage(message.getChannel(), embed.build());
    }

    @Override
    public String getDescription () {

        return "Generates stats for a creator on Curse.";
    }

}
