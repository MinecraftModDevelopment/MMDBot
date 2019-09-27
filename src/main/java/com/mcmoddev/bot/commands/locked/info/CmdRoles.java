package com.mcmoddev.bot.commands.locked.info;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;

public class CmdRoles extends Command {

    public CmdRoles() {
        name = "roles";
        aliases = new String[]{"roleinfo"};
        help = "Gives a count of users per role.";
    }

    @Override
    protected void execute(CommandEvent event) {
        Guild guild = event.getGuild();
        EmbedBuilder embed = new EmbedBuilder();
        TextChannel channel = event.getTextChannel();

        //Get the amount of people who have the role specified.
        int STAFF = guild.getMembersWithRoles(guild.getRoleById("218607518048452610")).size();
        int PARTNER = guild.getMembersWithRoles(guild.getRoleById("252880821382283266")).size();
        int COMMUNITY_REPS = guild.getMembersWithRoles(guild.getRoleById("286223615765118986")).size();
        int MODDERS = guild.getMembersWithRoles(guild.getRoleById("191145754583105536")).size();
        int ARTIST = guild.getMembersWithRoles(guild.getRoleById("179305517343047680")).size();
        int STREAMER = guild.getMembersWithRoles(guild.getRoleById("219679192462131210")).size();
        int MODPACK_MAKERS = guild.getMembersWithRoles(guild.getRoleById("215403201090813952")).size();
        int TRANSLATOR = guild.getMembersWithRoles(guild.getRoleById("201471697482678273")).size();
        int BOOSTER = guild.getMembersWithRoles(guild.getRoleById("590166091234279465")).size();

        embed.setColor(Color.GREEN);
        embed.setTitle("Users With Role");
        embed.setDescription("A count of how many members have been assigned some of MMD's many roles.");

        embed.addField("Staff count:", Integer.toString(STAFF), true);
        embed.addField("Partner count:", Integer.toString(PARTNER), true);
        embed.addField("Community Rep count:", Integer.toString(COMMUNITY_REPS), true);
        embed.addField("Modder count:", Integer.toString(MODDERS), true);
        embed.addField("Artist count:", Integer.toString(ARTIST), true);
        embed.addField("Streamer count:", Integer.toString(STREAMER), true);
        embed.addField("Modpack Maker count:", Integer.toString(MODPACK_MAKERS), true);
        embed.addField("Translator count:", Integer.toString(TRANSLATOR), true);
        embed.addField("Nitro Booster count:", Integer.toString(BOOSTER), true);
        embed.setTimestamp(Instant.now());

        if (channel.getIdLong() != BotConfig.getConfig().getBotStuffChannelId()) {
            channel.sendMessage("This command is channel locked to <#572261616956211201>");
        } else {
            channel.sendMessage(embed.build()).queue();
        }
    }
}
