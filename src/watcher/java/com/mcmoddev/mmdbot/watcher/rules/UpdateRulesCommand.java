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

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.core.util.webhook.WebhookManager;
import com.mcmoddev.mmdbot.watcher.TheWatcher;
import com.mcmoddev.mmdbot.watcher.util.database.RulesDAO;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class UpdateRulesCommand extends SlashCommand {
    public static final WebhookManager WEBHOOKS = WebhookManager.of("Rules");

    public static final String ACCEPTED_RULES_ROLE_KEY = "rules-agreed-role";
    public static final String RULES_CHANNEL_KEY = "rules-channel";
    public static final String RULE_KEY = "rule%s";

    public UpdateRulesCommand() {
        name = "updaterules";
        help = "Updates the server's rules.";
        guildOnly = true;
        userPermissions = new Permission[] {
            Permission.MANAGE_ROLES // TODO: maybe admin?
        };
        botPermissions = new Permission[] {
            Permission.MANAGE_ROLES
        };

        options = List.of(
            new OptionData(OptionType.CHANNEL, "channel", "The server's rules channel", true),
            new OptionData(OptionType.STRING, "rules", "An URL to the raw rules in md format", true),
            new OptionData(OptionType.ROLE, "role", "The 'Rules agreed' role", true)
        );
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var channel = Objects.requireNonNull(event.getOption("channel", OptionMapping::getAsChannel)).asTextChannel();
        final var role = Objects.requireNonNull(event.getOption("role", OptionMapping::getAsRole));
        final var rules = Objects.requireNonNull(event.getOption("rules", OptionMapping::getAsString));

        final String data;
        try (final var is = new URL(rules).openStream()) {
            data = new String(is.readAllBytes());
        } catch (IOException e) {
            event.deferReply(true)
                .setContent("There was an exception running the command: " + e.getLocalizedMessage())
                .queue();
            return;
        }
        event.deferReply().queue();
        TheWatcher.getInstance().getJdbi().useExtension(RulesDAO.class, db -> {
            db.clear(event.getGuild().getIdLong());
            db.insert(event.getGuild().getIdLong(), ACCEPTED_RULES_ROLE_KEY, role.getId());
            db.insert(event.getGuild().getIdLong(), RULES_CHANNEL_KEY, channel.getId());
        });

        final Runnable whenDeleted = () -> {
            final var messages = TheWatcher.getInstance().getJdbi().withExtension(RulesDAO.class,
                db -> RuleParser.parse(data, (index, rule) -> db.insert(event.getGuild().getIdLong(), RULE_KEY.formatted(index), rule.toJson())));
            final var webhook = WEBHOOKS.getWebhook(channel);
            final Function<MessageCreateData, WebhookMessage> function = jda -> {
                final var builder = new WebhookMessageBuilder()
                    .setContent(jda.getContent())
                    .setAvatarUrl(event.getGuild().getIconUrl())
                    .setUsername(event.getGuild().getName())
                    .setAllowedMentions(AllowedMentions.none());
                jda.getEmbeds().forEach(embed -> builder.addEmbeds(WebhookEmbedBuilder.fromJDA(embed).build()));
                return builder.build();
            };
            if (messages.isEmpty()) {
                event.getHook().sendMessage("Could not update rules! Empty list of messages was gathered.").setEphemeral(true).queue();
                return;
            }
            CompletableFuture<?> action = webhook.send(function.apply(messages.get(0)));
            for (int i = 1; i < messages.size(); i++) {
                final var msg = messages.get(i);
                action = action.thenCompose(sent -> webhook.send(function.apply(msg)));
            }
            action.whenComplete(($$, e) -> {
                if (e != null) {
                    TheWatcher.LOGGER.error("Exception trying to update rules: ", e);
                    event.getHook().sendMessage("There was an exception executing that command: " + e.getLocalizedMessage())
                        .setEphemeral(true).queue();
                } else {
                    channel.sendMessage("Click the button bellow to be able to talk in the server.")
                        .setActionRow(Button.of(
                            ButtonStyle.PRIMARY, "rules-accept-start", "Click me!"
                        ))
                        .flatMap($$$$ -> event.getHook().sendMessage("Successfully updated the rules!").setEphemeral(true))
                        .queue();
                }
            });
        };
        if (channel.getLatestMessageIdLong() != 0) {
            channel.getHistoryBefore(channel.getLatestMessageIdLong(), 100)
                .map(MessageHistory::getRetrievedHistory)
                .submit()
                .thenApply(messages -> {
                    if (messages.size() >= 2) {
                        return CompletableFuture.allOf(channel.purgeMessages(messages)
                            .toArray(new CompletableFuture[0]))
                            .thenApply(s -> channel.deleteMessageById(channel.getLatestMessageId()).submit());
                    } else if (!messages.isEmpty()) {
                        return RestAction.allOf(messages.stream().map(Message::delete).toList()).submit()
                            .thenApply(s -> channel.deleteMessageById(channel.getLatestMessageId()).submit());
                    } else {
                        return channel.deleteMessageById(channel.getLatestMessageId()).submit();
                    }
                })
                .whenComplete(($1, $2) -> whenDeleted.run());
        } else {
            whenDeleted.run();
        }
    }

    public static void onEvent(final GenericEvent gEvent) {
        if (!(gEvent instanceof ButtonInteractionEvent event)) return;
        if (event.getButton().getId() == null || event.getGuild() == null || event.getMember() == null) return;

        final var role = event.getGuild().getRoleById(getAcceptedRulesRole(event.getGuild().getIdLong()));
        if (role == null) {
            event.reply("The server has not configured a valid 'Rules Agreed' rule, please contact server admins.")
                .setEphemeral(true)
                .queue();
            return;
        }

        if (event.getButton().getId().equals("rules-accept-start")) {
            event.reply("By clicking the button below, you accept that you have read the rules, you agree to them, and you will follow them in every interaction in the server.")
                .addActionRow(Button.of(
                    ButtonStyle.SECONDARY, "rules-agreed", "\uD83D\uDCDA I agree to the rules"
                ), Button.of(
                    ButtonStyle.SUCCESS, "rules-accept-denied", "\uD83D\uDEAE️ I refuse to accept the rules"
                ))
                .setEphemeral(true)
                .queue();
        } else if (event.getButton().getId().equals("rules-agreed")) {
            if (event.getMember().getRoles().stream().anyMatch(it -> it.getIdLong() == role.getIdLong())) {
                event.reply("You have already agreed to the rules!").setEphemeral(true).queue();
                return;
            }
            event.getGuild().modifyMemberRoles(event.getMember(), List.of(role), null)
                .flatMap(v -> event.reply("Role granted! Have fun in the server, and remember to abide to the rules!")
                    .setEphemeral(true))
                .queue();
        } else if (event.getButton().getId().equals("rules-accept-denied")) {
            event.reply("Okay then, bye!")
                .setEphemeral(true)
                .delay(3, TimeUnit.SECONDS)
                .flatMap(hook -> event.getMember().kick("Refusing to accept the rules."))
                .queue();
        }
    }

    public static long getAcceptedRulesRole(long guildId) {
        final var val = TheWatcher.getInstance().getJdbi().withExtension(RulesDAO.class, it -> it.get(guildId, ACCEPTED_RULES_ROLE_KEY));
        if (val == null) return 0L;
        try {
            return Long.parseLong(val);
        } catch (Exception ignored) {
            return 0L;
        }
    }

    public static String getRulesChannel(Guild guild) {
        return TheWatcher.getInstance().getJdbi().withExtension(RulesDAO.class, it -> it.get(guild.getIdLong(), RULES_CHANNEL_KEY));
    }
}
