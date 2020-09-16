package com.mcmoddev.mmdbot.commands.info.server;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class CmdRoles extends Command {

    /**
     *
     */
    public CmdRoles() {
        super();
        name = "roles";
        aliases = new String[]{"roleinfo", "server-roles"};
        help = "Gives a count of users per role.";
    }

    /**
     * Get the amount of members per role.
     *
     * @param guild The guild we are in.
     * @param id    The ID of the role.
     * @return
     */
    private String getRoleMemberCount(final Guild guild, final String id) {
        return Integer.toString(guild.getMembersWithRoles(guild.getRoleById(id)).size());
    }

    /**
     *
     */
    @Override
    protected void execute(final CommandEvent event) {
        final Guild guild = event.getGuild();
        final Map<String, String> roleList = new HashMap<>();
        final EmbedBuilder embed = new EmbedBuilder();
        final TextChannel channel = event.getTextChannel();

        for (Role roles : event.getGuild().getRoles()) {
            try {
                if (!roleList.containsKey(roles.getName())) {
                    roleList.put(roles.getName(), getRoleMemberCount(guild, roles.getId()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        embed.setColor(Color.GREEN);
        embed.setTitle("Users With Role");
        embed.setDescription("A count of how many members have been assigned some of MMD's many roles.");
        embed.addField("Role count:", Utils.mapToString(Utils.sortByValue(roleList, true)), true);
        embed.setTimestamp(Instant.now());
        channel.sendMessage(embed.build()).queue();
    }
}
