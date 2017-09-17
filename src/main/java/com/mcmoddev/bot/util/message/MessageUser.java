package com.mcmoddev.bot.util.message;

import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class MessageUser extends EmbedBuilder {

    public MessageUser (IUser user, IGuild guild) {

        super();

        this.setLenient(true);

        this.withTitle(user.getName());

        this.appendField("Tag", Utilities.getUserTag(user), true);
        this.appendField("ID", "" + user.getLongID(), true);
        this.appendField("Created", Utilities.formatTime(user.getCreationDate()), true);

        if (guild != null) {

            this.appendField("Joined", Utilities.formatTime(guild.getJoinTimeForUser(user)), true);
            this.appendField("Nickname", user.getDisplayName(guild), true);
        }

        this.withThumbnail(user.getAvatarURL());
    }
}
