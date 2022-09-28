/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
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
package com.mcmoddev.mmdbot.watcher.rules;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import com.mcmoddev.mmdbot.watcher.util.database.RulesDAO;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import javax.annotation.Nullable;
import java.util.List;

public class RuleCommand extends SlashCommand {
    public static final RuleCommand INSTANCE = new RuleCommand();
    private RuleCommand() {
        name = "rule";
        help = "Gets a rule by its number";
        options = List.of(new OptionData(
            OptionType.INTEGER, "rule", "The number of the rule to get", true
        ));
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final int id = event.getOption("rule", 1, OptionMapping::getAsInt);
        final var rule = getRule(event.getGuild(), id);
        if (rule == null) {
            event.reply("Unknown rule nr. " + id)
                .setEphemeral(true)
                .queue();
        } else {
            event.reply(new MessageCreateBuilder()
                .setEmbeds(rule.asEmbed(id)
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .build())
                .setContent("See the server's rules in <#%s>.".formatted(UpdateRulesCommand.getRulesChannel(event.getGuild())))
                .build())
                .queue();
        }
    }

    @Override
    protected void execute(final CommandEvent event) {
        final int id;
        try {
            id = Integer.parseInt(event.getArgs().trim());
        } catch (NumberFormatException ignored) {
            event.reply("Provided argument is not a number!");
            return;
        }
        final var rule = getRule(event.getGuild(), id);
        if (rule == null) {
            event.reply("Unknown rule nr. " + id);
        } else {
            event.reply(new MessageCreateBuilder()
                .setEmbeds(rule.asEmbed(id)
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .build())
                .setContent("See the server's rules in <#%s>.".formatted(UpdateRulesCommand.getRulesChannel(event.getGuild())))
                .build());
        }
    }

    @Nullable
    protected RuleData getRule(Guild guild, int id) {
        final var data = TheWatcher.getInstance().getJdbi().withExtension(RulesDAO.class, db -> db.get(guild.getIdLong(), UpdateRulesCommand.RULE_KEY.formatted(id)));
        if (data == null) return null;
        return RuleData.from(data);
    }
}
