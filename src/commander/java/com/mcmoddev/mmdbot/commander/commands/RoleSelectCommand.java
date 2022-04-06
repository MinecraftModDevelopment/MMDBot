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
package com.mcmoddev.mmdbot.commander.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.ModalInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.context.SelectMenuInteractionContext;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class RoleSelectCommand extends SlashCommand {

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
        options = List.of(
            new OptionData(OptionType.STRING, "roles", "Mention the roles to add to the panel.", true),
            new OptionData(OptionType.BOOLEAN, "dropdown", "If the role selection panel should be a dropdown rather than buttons.")
        );
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var selfMember = Objects.requireNonNull(event.getGuild()).getSelfMember();
        Objects.requireNonNull(event.getMember());
        final var args = event.getOption("roles", List.<String>of(), m -> m.getMentionedRoles()
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
        args.add(0, String.valueOf(event.getOption("dropdown", false, OptionMapping::getAsBoolean)));

        final var title = TextInput.create("title", "Title", TextInputStyle.SHORT)
            .setRequired(false)
            .setMaxLength(MessageEmbed.TITLE_MAX_LENGTH)
            .setPlaceholder("The title of the role panel to create.")
            .build();

        final var description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
            .setRequired(false)
            .setMaxLength(Math.min(MessageEmbed.DESCRIPTION_MAX_LENGTH, TextInput.TEXT_INPUT_MAX_LENGTH))
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

        final var message = new MessageBuilder()
            .setEmbeds(
                new EmbedBuilder()
                    .setTitle(title)
                    .setColor(colour)
                    .setDescription(description)
                    .build()
            )
            .setActionRows();

        if (isDropdown) {
            handleDropdownCreation(context, message, roles);
        } else {
            handleButtonOption(context, message, roles);
        }
    }

    protected void onSelectMenu(final SelectMenuInteractionContext context) {
        final var event = context.getEvent();
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

    private static void handleButtonOption(final @NonNull ModalInteractionContext context, final MessageBuilder builder, final List<Role> selectedRoles) {
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
            .sendMessage(builder.setActionRows(rows).build())
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

    private static void handleDropdownCreation(final @NonNull ModalInteractionContext context, final MessageBuilder message, final List<Role> selectedRoles) {
        SelectMenu.Builder menu = COMPONENT_LISTENER.createMenu(Component.Lifespan.PERMANENT)
            .setPlaceholder("Select your roles")
            .setMaxValues(selectedRoles.size())
            .setMinValues(0);

        selectedRoles.forEach(role -> menu.addOption(role.getName(), role.getId()));

        context.getEvent().getMessageChannel()
            .sendMessage(message
                .setActionRows(ActionRow.of(menu.build()))
                .build())
            .flatMap($ -> context.getEvent().reply("Message created and sent successfully!").setEphemeral(true))
            .queue();
    }

    private static void handleRoleSelection(final @NonNull SelectMenuInteractionContext context, final @NonNull Collection<Role> selectedRoles, final Guild guild) {
        final var member = Objects.requireNonNull(context.getMember());
        final var toAdd = new ArrayList<Role>(selectedRoles.size());
        final var toRemove = new ArrayList<Role>(selectedRoles.size());

        context.getEvent().getInteraction()
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
}
