package com.mcmoddev.mmdbot.commands.staff;

import com.google.common.collect.Sets;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.MMDBot;
import com.mcmoddev.mmdbot.core.Utils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author
 *
 */
public final class CmdCommunityChannel extends Command {

	/**
	 *
	 */
    private static final EnumSet<Permission> REQUIRED_PERMISSIONS = EnumSet.of(Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL);

    /**
     *
     */
    public CmdCommunityChannel() {
        super();
        name = "create-community-channel";
        aliases = new String[]{"community-channel", "comm-ch"};
        help = "Creates a new community channel for the given user. Usage: !mmd-create-community-channel <user ID/mention> <channel name>";
        hidden = true;
        botPermissions = REQUIRED_PERMISSIONS.toArray(new Permission[REQUIRED_PERMISSIONS.size()]);
    }

    /**
     * @param event The {@link CommandEvent CommandEvent} that triggered this Command.
     */
    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }
        final Guild guild = event.getGuild();
        final Member author = guild.getMember(event.getAuthor());
        if (author == null) {
            return;
        }

        if (!author.hasPermission(REQUIRED_PERMISSIONS)) {
            event.reply("You do not have permission to use this command.");
            return;
        }

        final String[] args = event.getArgs().split(" ");
        final Member newOwner = Utils.getMemberFromString(args[0], event.getGuild());
        if (newOwner == null) {
            event.reply("Invalid user!");
            return;
        }

        final long categoryID = MMDBot.getConfig().getCommunityChannelCategory();
        final net.dv8tion.jda.api.entities.Category category = guild.getCategoryById(categoryID);
        if (categoryID == 0 || category == null) {
            MMDBot.LOGGER.warn("Community channel category is incorrectly configured");
            event.reply("Community channel category is incorrectly configured. Please contact the bot maintainers.");
            return;
        }

        final EnumSet<Permission> ownerPermissions = MMDBot.getConfig().getCommunityChannelOwnerPermissions();
        if (ownerPermissions.isEmpty()) {
            MMDBot.LOGGER.warn("Community channel owner permissions is incorrectly configured");
            event.reply("Channel owner permissions is incorrectly configured. Please contact the bot maintainers.");
            return;
        }

        final Set<Permission> diff = Sets.difference(ownerPermissions, event.getSelfMember().getPermissions());
        if (!diff.isEmpty()) {
            MMDBot.LOGGER.warn("Cannot assign permissions to channel owner due to insufficient permissions: {}", diff);
            event.reply("Cannot assign certain permissions to channel owner due to insufficient permissions; continuing anyway...");
            ownerPermissions.removeIf(diff::contains);
        }

        final List<Emote> emote = new ArrayList<>(4);
        emote.addAll(guild.getEmotesByName("mmd1", true));
        emote.addAll(guild.getEmotesByName("mmd2", true));
        emote.addAll(guild.getEmotesByName("mmd3", true));
        emote.addAll(guild.getEmotesByName("mmd4", true));

        // Flavor text: if the emotes are available, use them, else just use plain MMD
        final String emoteText = emote.size() == 4 ? emote.stream().map(Emote::getAsMention).collect(Collectors.joining()) : "";
        final String flavorText = emoteText.isEmpty() ? "MMD" : emoteText;

        final String channelName = args[1];
        MMDBot.LOGGER.info("Creating new community channel for {}, named \"{}\" (command issued by {})", newOwner, channelName, author);
        category.createTextChannel(channelName)
            .flatMap(ch -> category.modifyTextChannelPositions()
                .sortOrder(Comparator.comparing(GuildChannel::getName))
                .map($ -> ch))
            .flatMap(ch -> ch.putPermissionOverride(newOwner).setAllow(ownerPermissions).map($ -> ch))
            .flatMap(ch -> ch.sendMessage(new MessageBuilder()
                .appendFormat("Welcome %s to your new community channel, %s!%n", newOwner.getAsMention(), ch.getAsMention())
                .append('\n')
                .append("Please adhere to the Code of Conduct of the server")
                .appendFormat(" (which can be visited from the <#%s> channel),", MMDBot.getConfig().getChannel("info.rules"))
                .append(" specifically the Channel Policy section.").append('\n')
                .append('\n')
                .appendFormat("Thank you, and enjoy your new home here at %s!", flavorText)
                .build())
                .map($ -> ch)
            )
            .queue(c -> event.reply("Successfully created community channel at " + c.getAsMention() + "!"));

    }
}
