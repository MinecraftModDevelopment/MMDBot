package com.mcmoddev.mmdbot.core.util.jda;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import java.util.List;

public interface EnabledRolesCommand {
    List<Long> getEnabledRoles();

    default boolean checkCanUse(final SlashCommandEvent event) {
        if (event.getMember().getRoles().stream().noneMatch(r -> getEnabledRoles().contains(r.getIdLong()))) {
            event.deferReply(true).setContent("You do not have the required role to use this command!");
            return false;
        }
        return true;
    }

    abstract class Base extends SlashCommand implements EnabledRolesCommand {
        protected List<Long> enabledRoles;

        @Override
        public List<Long> getEnabledRoles() {
            return enabledRoles;
        }
    }
}
