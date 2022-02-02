/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.modules.commands.community.server.tricks;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.modules.commands.CommandModule;
import com.mcmoddev.mmdbot.modules.commands.community.server.DeletableCommand;
import com.mcmoddev.mmdbot.utilities.Utils;
import com.mcmoddev.mmdbot.utilities.tricks.Trick;
import com.mcmoddev.mmdbot.utilities.tricks.Tricks;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Runs a certain trick.
 * Takes zero or one parameters, the argument string to be passed to the trick.
 * <p>
 * Takes the form:
 * /test
 * /[trickname] [args]
 *
 * @author Will BL
 * @author Curle
 * @deprecated This system is bad, and such I added autocomplete to `/trick run` - matyrobbrt
 */
@Deprecated(forRemoval = true)
public final class CmdRunTrickSeparated extends SlashCommand implements DeletableCommand {

    private final String trickName;
    private boolean deleted = false;

    /**
     * Instantiates a new command for a certain trick.
     */
    public CmdRunTrickSeparated(String trickName) {
        super();
        this.trickName = trickName;
        name = trickName;
        aliases = Tricks.getTrick(trickName).map(t -> t.getNames()).orElse(new ArrayList<>()).toArray(String[]::new);
        help = "Invoke the trick " + trickName;
        category = new Category("Fun");
        guildOnly = true;
        options = Collections.singletonList(new OptionData(OptionType.STRING, "args", "The arguments for the trick, if any").setRequired(false));
    }

    public CmdRunTrickSeparated(Trick trick) {
        this(trick.getNames().get(0));
    }

    /**
     * Execute.
     *
     * @param event the event
     */
    @Override
    protected void execute(final SlashCommandEvent event) {
        if (!Utils.checkCommand(this, event)) {
            return;
        }

        Tricks.getTrick(trickName).ifPresentOrElse(trick -> event.reply(trick.getMessage(Utils.getOrEmpty(event, "args").split(" "))).setEphemeral(false).queue(),
            () -> event.deferReply(true).setContent("This trick does not exist anymore!").queue());
    }

    @Override
    public void delete() {
        deleted = true;
    }

    @Override
    public void restore() {
        deleted = false;
        CommandModule.upsertCommand(this);
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }
}
