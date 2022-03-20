package com.mcmoddev.mmdbot.commander.commands.menu.user;

import com.jagrosh.jdautilities.command.UserContextMenu;
import com.jagrosh.jdautilities.command.UserContextMenuEvent;
import com.mcmoddev.mmdbot.commander.eventlistener.DismissListener;
import com.mcmoddev.mmdbot.commander.util.TheCommanderUtilities;

public class UserInfoContextMenu extends UserContextMenu {

    public UserInfoContextMenu() {
        name = "User Info";
    }

    @Override
    protected void execute(final UserContextMenuEvent event) {
        if (!event.isFromGuild()) {
            event.deferReply(true).setContent("This command can only be used in a guild!");
            return;
        }
        final var embed = TheCommanderUtilities.createMemberInfoEmbed(event.getTargetMember());
        event.replyEmbeds(embed.build()).addActionRow(DismissListener.createDismissButton(event))
            .mentionRepliedUser(false).queue();
    }
}
