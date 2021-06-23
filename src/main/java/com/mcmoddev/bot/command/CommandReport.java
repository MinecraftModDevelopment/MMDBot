package com.mcmoddev.bot.command;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.database.Report;
import com.mcmoddev.bot.util.Utilities;
import com.mcmoddev.bot.util.StringUtilities;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class CommandReport implements Command {

    //TODO Move to config
    public static final int REPORT_THRESHOLD = 20;
    public static final int REPORT_EXPIRED_THRESHOLD = 30;

    @Override
    public void processCommand(IMessage message, String[] params) {
        if (params.length > 2) {
            switch (params[1]) {
                case "create": {
                    if (params.length > 4) {
						// TODO: FIX required
                        IUser user = null; // message.getGuild().getUserByID(params[2].replaceAll("[^0-9]", ""));
                        String points = params[3];
                        String desc = StringUtilities.arrayToString(params, 4, " ");

                        int reportID = MMDBot.database.createReport(user.getDiscriminator(), points, desc, message.getTimestamp(), message.getAuthor().getDiscriminator());
                        int count = MMDBot.database.getPointCount(user.getDiscriminator(), false);
                        int expired = MMDBot.database.getPointCount(user.getDiscriminator(), true);

                        Utilities.sendMessage(message.getChannel(), String.format("This report with ID %s has been made against %s, this report can be deleted or edited with the ID.", reportID, user.getDisplayName(message.getGuild())));

                        if (count >= REPORT_THRESHOLD) {
                            Utilities.sendMessage(message.getChannel(), String.format("This user has %s points on their record which expired the threshold of %s, an admin has been notified for further review.", count, REPORT_THRESHOLD));
                            //TODO Send message to admin channel
                            return;
                        }

                        if (expired >= REPORT_EXPIRED_THRESHOLD) {
                            Utilities.sendMessage(message.getChannel(), String.format("This user has %s points on their record with expired with the expired threshold of %s, an admin has been notified for further review.", expired, REPORT_EXPIRED_THRESHOLD));
                            //TODO Send message to admin channel
                            return;
                        }

                    }
                    break;
                }
                case "list":
                case "offenses": {
                    if (params.length == 3) {
						// TODO: Fix Required
                        //IUser user = message.getGuild().getUserByID(params[2].replaceAll("[^0-9]", ""));
                        //Report[] report = MMDBot.database.getReports(user);

                    }
                    break;
                }

                case "edit": {
                    if (params.length > 3) {
                        String ID = params[2];
                        String desc = StringUtilities.arrayToString(params, 3, " ");
                        Report report = MMDBot.database.deleteReport(ID);

                        int reportID = MMDBot.database.createReport(report.getUserID(), String.valueOf(report.getPoints()), desc, message.getTimestamp(), report.getReporter());
                        Utilities.sendMessage(message.getChannel(), String.format("The report has been edited, a new ID has been assigned under %s", reportID));

                    }
                    break;
                }
                case "delete": {
                    if (params.length == 3) {
                        String ID = params[2];
                        MMDBot.database.deleteReport(ID);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return null;
    }
}
