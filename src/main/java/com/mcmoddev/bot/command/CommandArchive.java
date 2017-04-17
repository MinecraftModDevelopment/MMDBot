package com.mcmoddev.bot.command;

import org.apache.commons.lang3.SystemUtils;
import org.kohsuke.github.GHGist;

import com.mcmoddev.bot.handlers.GitHubHandler;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageHistory;

public class CommandArchive extends CommandAdmin {

    @Override
    public void processCommand (IMessage message, String[] params) {

        try {
            if (params.length < 2) {
                Utilities.sendMessage(message.getChannel(), "Please specify a channel name!");
                return;
            }

            final IChannel channel = message.getGuild().getChannelsByName(params[1]).get(0);

            long time = System.currentTimeMillis();
            Utilities.sendMessage(message.getChannel(), "Starting. This may take a long time!");

            final MessageHistory history = message.getChannel().getFullMessageHistory();

            final StringBuilder builder = new StringBuilder();

            for (final IMessage log : history)
                builder.append(log.getTimestamp().toString() + " " + Utilities.userString(log.getAuthor()) + " " + log.getContent() + SystemUtils.LINE_SEPARATOR);

            final GHGist file = GitHubHandler.createGist(false, "Logs for " + channel.getName() + " taken at " + message.getTimestamp().toString(), channel.getName() + ".log", builder.toString());

            time = System.currentTimeMillis() - time;

            Utilities.sendMessage(message.getChannel(), "Archived messages for " + channel.getName() + " in " + time / 1000 + " seconds. " + file.getGitPullUrl());
        }

        catch (final Exception e) {

            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription () {

        return "Archives the contents of a channel and sends it to the user as a gist file.";
    }
}