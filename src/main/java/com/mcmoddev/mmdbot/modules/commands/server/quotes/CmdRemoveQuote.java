/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2021 <MMD - MinecraftModDevelopment>
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
package com.mcmoddev.mmdbot.modules.commands.server.quotes;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mcmoddev.mmdbot.core.Utils;
import com.mcmoddev.mmdbot.utilities.quotes.QuoteList;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Remove a quote from the list.
 * Requires an int argument.
 *
 * Possible forms:
 *
 * !removequote 5
 * Can only be used by Bot Maintainers and higher.
 *
 * @author Curle
 */
public final class CmdRemoveQuote extends Command {

    /**
     * Create the command.
     * Sets all the usual flags.
     */
    public CmdRemoveQuote() {
        super();
        name = "removequote";
        aliases = new String[]{"quote-remove", "remove-quote"};
        help = "Remove a quote from the list.";
        requiredRole = "bot maintainer";
    }

    @Override
    protected void execute(final CommandEvent event) {
        if (!Utils.checkCommand(this, event))
            return;

        final TextChannel channel = event.getTextChannel();
        String argsFull = event.getArgs();
        if (argsFull.length() > 0) {
            // We have something to parse.
            try {
                int index = Integer.parseInt(argsFull.trim());
                QuoteList.removeQuote(index);
                channel.sendMessage("Quote " + index + " removed.").queue();
            } catch (NumberFormatException ignored) {
                channel.sendMessage("Unable to determine which quote to remove.").queue();
            }
        } else {
            channel.sendMessage("Specify a quote ID to remove.").queue();
        }
    }
}
