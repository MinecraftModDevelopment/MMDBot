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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.util.dao.ReactionRolePanels;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.ModalInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.SelectMenuInteractionContext;
import com.mcmoddev.mmdbot.core.util.Utils;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectInteraction;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RoleSelectCommand extends SlashCommand implements EventListener {

    @RegisterSlashCommand
    public static final RoleSelectCommand COMMAND = new RoleSelectCommand();

    public static final ComponentListener COMPONENT_LISTENER = TheCommander.getComponentListener("role-select")
        .onSelectMenuInteraction(COMMAND::onSelectMenu)
        .onButtonInteraction(COMMAND::onButtonInteraction)
        .onModalInteraction(COMMAND::onModalInteraction)
        .build();

    private RoleSelectCommand() {
        this.name = "role-select";
        this.guildOnly = true;
        help = "Creates selection menus for roles.";
        botPermissions = new Permission[]{
            Permission.MANAGE_ROLES
        };
        userPermissions = new Permission[]{
            Permission.MANAGE_ROLES
        };
        children = new SlashCommand[]{
            command("buttons", "Creates a role panel with buttons.", event -> executeSub(event, false), new OptionData(OptionType.STRING, "roles", "Mention the roles to add to the panel.", true)),
            command("dropdown", "Creates a dropdown role panel.", event -> executeSub(event, true), new OptionData(OptionType.STRING, "roles", "Mention the roles to add to the panel.", true)),
            command("reaction", "Creates a reaction role panel.", event -> {
                    try {
                        Objects.requireNonNull(event.getGuild());
                        var channel = event.getMessageChannel();
                        var msgIdStr = event.getOption("message", "", OptionMapping::getAsString);
                        if (msgIdStr.contains("-")) {
                            // Accept the `channelID-messageID` format Discord gives when using the Copy ID button
                            final var newChannel = event.getGuild().getTextChannelById(msgIdStr.substring(0, msgIdStr.indexOf('-')));
                            if (newChannel != null) {
                                channel = newChannel;
                            }
                            msgIdStr = msgIdStr.substring(msgIdStr.indexOf('-') + 1);
                        }
                        final var channelId = channel.getIdLong(); // Field must be final for lambda usage
                        final long messageId = Long.parseLong(msgIdStr);
                        final var role = Objects.requireNonNull(event.getOption("role", OptionMapping::getAsRole));
                        final var emote = Emoji.fromFormatted(event.getOption("emote", "", OptionMapping::getAsString).replace(" ", ""));
                        final var emoteStr = getEmoteAsString(emote);

                        channel.addReactionById(messageId, emote).flatMap($ -> {
                            // Emote IDs are expected, but the emoteStr is also prefixed with :emojiName:
                            TheCommander.getInstance()
                                .getJdbi()
                                .useExtension(ReactionRolePanels.class, db -> db.insert(channelId, messageId, emote.getType() == Emoji.Type.CUSTOM ? ((CustomEmoji) emote).getId() : emoteStr, role.getIdLong(), event.getOption("permanent", false, OptionMapping::getAsBoolean)));
                            return event.deferReply(true)
                                .addEmbeds(new EmbedBuilder()
                                    .setColor(Color.GREEN)
                                    .setDescription("Action successful. [Jump to message.](%s)"
                                        .formatted(Utils.makeMessageLink(event.getGuild().getIdLong(), channelId, messageId)))
                                    .build())
                                .mentionRepliedUser(false);
                        }).queue(null, e -> event.deferReply(true).setContent("There was an exception while trying to execute that command: **%s**"
                            .formatted(e.getLocalizedMessage())).mentionRepliedUser(false).queue());
                    } catch (Exception e) {
                        // Usually a NumberFormattingException, but in case something else throws
                        event.deferReply(true).setContent("There was an exception while trying to execute that command: **%s**"
                            .formatted(e.getLocalizedMessage())).mentionRepliedUser(false).queue();
                        TheCommander.LOGGER.error("There was an error running the `/role-select reaction` command", e);
                    }
                }, new OptionData(OptionType.STRING, "message", "The ID of the message on which to create the panel.", true),
                new OptionData(OptionType.ROLE, "role", "The role to add to the panel.", true),
                new OptionData(OptionType.STRING, "emote", "The emote which will be associated with that role.", true),
                new OptionData(OptionType.BOOLEAN, "permanent", "If the role assigned by the panel is permanent.")
            )
        };
    }

    protected void executeSub(final SlashCommandEvent event, final boolean dropdown) {
        final var selfMember = Objects.requireNonNull(event.getGuild()).getSelfMember();
        Objects.requireNonNull(event.getMember());
        final var args = event.getOption("roles", List.<String>of(), m -> m.getMentions().getRoles()
            .stream()
            .filter(r -> !r.isManaged() && selfMember.canInteract(r) && event.getMember().canInteract(r))
            .map(Role::getId)
            .collect(Collectors.toList()));

        if (args.isEmpty()) {
            event
                .deferReply(true)
                .setContent("Please provide at least one role for the panel.")
                .queue();
            return;
        } else if (args.size() > 25) {
            event
                .deferReply(true)
                .setContent("Please provide less than 25 roles for the panel.")
                .queue();
        }
        args.add(0, String.valueOf(dropdown));

        final var title = TextInput.create("title", "Title", TextInputStyle.SHORT)
            .setRequired(false)
            .setMaxLength(MessageEmbed.TITLE_MAX_LENGTH)
            .setPlaceholder("The title of the role panel to create.")
            .build();

        final var description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
            .setRequired(false)
            .setMaxLength(Math.min(MessageEmbed.DESCRIPTION_MAX_LENGTH, TextInput.MAX_VALUE_LENGTH))
            .setPlaceholder("The description role panel to create.")
            .build();

        final var colour = TextInput.create("colour", "Colour", TextInputStyle.SHORT)
            .setRequired(false)
            .setPlaceholder("The colour the embed will have. Example: #ffffff")
            .build();

        final var modal = COMPONENT_LISTENER.createModal("Role selection panel creation", Component.Lifespan.TEMPORARY, args)
            .addActionRows(
                ActionRow.of(title),
                ActionRow.of(description),
                ActionRow.of(colour)
            )
            .build();

        event.replyModal(modal).queue();
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
    }

    public void onModalInteraction(final ModalInteractionContext context) {
        final var event = context.getEvent();
        if (!event.isFromGuild()) return;
        Objects.requireNonNull(context.getGuild());

        final var titleOption = event.getValue("title");
        final var descriptionOption = event.getValue("description");
        final var colourOption = event.getValue("colour");

        final var title = titleOption == null ? null : titleOption.getAsString();
        final var description = descriptionOption == null ? null : descriptionOption.getAsString();
        final var colour = colourOption == null ? null : Color.decode(colourOption.getAsString());

        final var isDropdown = Boolean.parseBoolean(context.getArguments().get(0));
        final var roles = context.getArguments().subList(1, context.getArguments().size())
            .stream()
            .map(context.getGuild()::getRoleById)
            .toList();

        final var message = new MessageCreateBuilder()
            .setEmbeds(
                new EmbedBuilder()
                    .setTitle(title)
                    .setColor(colour)
                    .setDescription(description)
                    .build()
            )
            .setComponents();

        if (isDropdown) {
            handleDropdownCreation(context, message, roles);
        } else {
            handleButtonOption(context, message, roles);
        }
    }

    protected void onSelectMenu(final SelectMenuInteractionContext context) {
        final var gev = context.getEvent();
        if (!(gev.getInteraction() instanceof StringSelectInteraction event)) return;
        final var guild = Objects.requireNonNull(event.getGuild());
        final var selfMember = guild.getSelfMember();
        final var selectedRoles = event.getSelectedOptions()
            .stream()
            .map(SelectOption::getValue)
            .map(guild::getRoleById)
            .filter(Objects::nonNull)
            .filter(selfMember::canInteract)
            .toList();

        handleRoleSelection(context, selectedRoles, guild);
    }

    private static void handleButtonOption(final @NonNull ModalInteractionContext context, final MessageCreateBuilder builder, final List<Role> selectedRoles) {
        final UUID id = UUID.randomUUID();
        final var idString = id.toString();
        final List<ActionRow> rows = new ArrayList<>();
        final List<Role> tempRoles = new ArrayList<>();
        for (final Role role : selectedRoles) {
            tempRoles.add(role);
            if (tempRoles.size() == 5) {
                rows.add(ActionRow.of(
                    tempRoles.stream().map(r -> createButtonForRole(idString, r))
                        .toList()
                ));
                tempRoles.clear();
            }
        }
        if (tempRoles.size() > 0) {
            rows.add(ActionRow.of(
                tempRoles.stream().map(r -> createButtonForRole(idString, r))
                    .toList()
            ));
        }

        final var component = new Component(COMPONENT_LISTENER.getName(), id, List.of(), Component.Lifespan.PERMANENT);
        COMPONENT_LISTENER.insertComponent(component);

        context.getEvent().getMessageChannel()
            .sendMessage(builder.setComponents(rows).build())
            .flatMap($ -> context.getEvent().reply("Message created and sent successfully!").setEphemeral(true))
            .queue();
    }

    private static Button createButtonForRole(final String id, final Role role) {
        final var icon = role.getIcon();
        final var bId = Component.createIdWithArguments(id, role.getId());
        if (icon != null && icon.isEmoji()) {
            return Button.of(ButtonStyle.PRIMARY, bId, role.getName(), Emoji.fromUnicode(Objects.requireNonNull(icon.getEmoji())));
        }
        return Button.primary(bId, role.getName());
    }

    private static void handleDropdownCreation(final @NonNull ModalInteractionContext context, final MessageCreateBuilder message, final List<Role> selectedRoles) {
        StringSelectMenu.Builder menu = COMPONENT_LISTENER.createMenu(Component.Lifespan.PERMANENT)
            .setPlaceholder("Select your roles")
            .setMaxValues(selectedRoles.size())
            .setMinValues(0);

        selectedRoles.forEach(role -> menu.addOption(role.getName(), role.getId()));

        context.getEvent().getMessageChannel()
            .sendMessage(message
                .setComponents(ActionRow.of(menu.build()))
                .build())
            .flatMap($ -> context.getEvent().reply("Message created and sent successfully!").setEphemeral(true))
            .queue();
    }

    private static void handleRoleSelection(final @NonNull SelectMenuInteractionContext context, final @NonNull Collection<Role> selectedRoles, final Guild guild) {
        final var member = Objects.requireNonNull(context.getMember());
        final var toAdd = new ArrayList<Role>(selectedRoles.size());
        final var toRemove = new ArrayList<Role>(selectedRoles.size());

        ((StringSelectInteraction)context.getEvent().getInteraction())
            .getComponent()
            .getOptions()
            .stream()
            .map(selectOption -> {
                final var role = guild.getRoleById(selectOption.getValue());

                if (null == role) {
                    TheCommander.LOGGER.warn(
                        "The {} ({}) role doesn't exist anymore but it is still an option in a selection menu!",
                        selectOption.getLabel(), selectOption.getValue());
                }

                return role;
            })
            .filter(Objects::nonNull)
            .forEach(role -> {
                if (selectedRoles.contains(role)) {
                    toAdd.add(role);
                } else {
                    toRemove.add(role);
                }
            });

        guild.modifyMemberRoles(member, toAdd, toRemove)
            .reason("Role Selection")
            .flatMap($ -> context.getEvent().reply("Successfully updated your roles!").setEphemeral(true))
            .queue();
    }

    protected void onButtonInteraction(@NonNull final ButtonInteractionContext context) {
        final var event = context.getEvent();
        final var roleId = context.getItemComponentArguments().get(0);
        final var guild = Objects.requireNonNull(event.getGuild());
        final var member = Objects.requireNonNull(event.getMember());
        final var role = guild.getRoleById(roleId);
        if (role == null) {
            TheCommander.LOGGER.warn(
                "The {} role doesn't exist anymore but it is still an option in a selection menu!",
                roleId);
            return;
        }
        if (member.getRoles().contains(role)) {
            guild.removeRoleFromMember(member, role)
                .reason("Role Selection")
                .flatMap($ -> event.deferReply(true).setContent("Successfully updated your roles!"))
                .queue();
        } else {
            guild.addRoleToMember(member, role)
                .reason("Role Selection")
                .flatMap($ -> event.deferReply(true).setContent("Successfully updated your roles!"))
                .queue();
        }
    }

    private static SlashCommand command(String cmdName, String description, Consumer<? super SlashCommandEvent> consumer, OptionData... cmdOptions) {
        return new SlashCommand() {
            {
                this.name = cmdName;
                this.help = description;
                this.options = List.of(cmdOptions);
            }

            @Override
            protected void execute(final SlashCommandEvent event) {
                consumer.accept(event);
            }
        };
    }

    public static String getEmoteAsString(final Emoji emoji) {
        return emoji.getType() == Emoji.Type.CUSTOM ? emoji.getFormatted().replaceAll("[<>]*", "")
            /* this will give the emoji in the format emojiName:emojiId */ :
            ((UnicodeEmoji) emoji).getAsCodepoints();
    }

    @Override
    public void onEvent(@NotNull final GenericEvent gE) {
        if (gE instanceof MessageReactionAddEvent addEvent) {
            doRolePanelStuff(addEvent, false);
        } else if (gE instanceof MessageReactionRemoveEvent removeEvent) {
            doRolePanelStuff(removeEvent, true);
        }
    }

    private void doRolePanelStuff(final GenericMessageReactionEvent event, final boolean isRemove) {
        if (!event.isFromGuild() || event.getUser() == null || event.getMember() == null || event.getUser().isBot() || event.getUser().isSystem()) {
            return;
        }
        final var emote = getEmoteAsString(event.getReaction());
        final var roleId = withExtension(db -> db.getRole(event.getChannel().getIdLong(), event.getMessageIdLong(), emote));
        if (roleId == null) return;
        final var role = event.getGuild().getRoleById(roleId);
        if (role != null) {
            final var member = event.getMember();
            if (isRemove && !withExtension(db -> db.isPermanent(event.getChannel().getIdLong(), event.getMessageIdLong(), emote))) {
                if (member.getRoles().contains(role)) {
                    event.getGuild().removeRoleFromMember(member, role).reason("Reaction Roles").queue();
                }
            } else if (!isRemove) {
                if (!member.getRoles().contains(role)) {
                    event.getGuild().addRoleToMember(member, role).reason("Reaction Roles").queue();
                }
            }
        }
    }

    public static String getEmoteAsString(final MessageReaction reactionEmote) {
        return reactionEmote.getEmoji().getType() == Emoji.Type.UNICODE ? ((UnicodeEmoji) reactionEmote.getEmoji()).getAsCodepoints()
            : ((CustomEmoji) reactionEmote.getEmoji()).getId();
    }

    public static <R> R withExtension(Function<ReactionRolePanels, R> callback) {
        return TheCommander.getInstance().getJdbi().withExtension(ReactionRolePanels.class, callback::apply);
    }
}
