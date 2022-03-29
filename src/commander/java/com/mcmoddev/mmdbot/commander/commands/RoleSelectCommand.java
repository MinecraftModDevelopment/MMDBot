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
import com.mcmoddev.mmdbot.core.commands.component.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.ModalInteractionContext;
import com.mcmoddev.mmdbot.core.commands.component.SelectMenuInteractionContext;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.Interaction;
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

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RoleSelectCommand extends SlashCommand {

    @RegisterSlashCommand
    public static final RoleSelectCommand COMMAND = new RoleSelectCommand();

    public static final ComponentListener COMPONENT_LISTENER = TheCommander.getComponentListener("role-select")
        .onSelectMenuInteraction(COMMAND::onSelectMenu)
        .onButtonInteraction(COMMAND::onButtonInteraction)
        .onModalInteraction(COMMAND::onModalInteraction)
        .build();

    private static final Color COLOUR = new Color(24, 221, 136, 255);

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
            new OptionData(OptionType.BOOLEAN, "dropdown", "If the role selection panel should be a dropdown rather than buttons.")
        );
    }

    @Override
    protected void execute(final SlashCommandEvent event) {
        final var title = TextInput.create("title", "Title", TextInputStyle.SHORT)
            .setRequired(false)
            .setPlaceholder("The title of the role panel to create.")
            .build();

        final var description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
            .setRequired(false)
            .setPlaceholder("The description role panel to create.")
            .build();

        // TODO implement mappings for role icons and add colour option

        final var modal = COMPONENT_LISTENER.createModal("Role selection panel creation", Component.Lifespan.TEMPORARY,
                String.valueOf(event.getOption("dropdown", false, OptionMapping::getAsBoolean)))
            .addActionRows(
                ActionRow.of(title),
                ActionRow.of(description)
            )
            .build();

        event.replyModal(modal).queue();
    }

    private static void addMenuOptions(@NonNull final Interaction interaction,
                                       @NonNull final SelectMenu.Builder menu,
                                       @NonNull final String placeHolder,
                                       @Nullable final Integer minValues) {

        final var guild = Objects.requireNonNull(interaction.getGuild());
        final var highestBotRole = guild.getSelfMember().getRoles().get(0);
        final var guildRoles = guild.getRoles();

        final var roles = guildRoles.subList(guildRoles.indexOf(highestBotRole) + 1, guildRoles.size());

        if (null != minValues) {
            menu.setMinValues(minValues);
        }

        menu.setPlaceholder(placeHolder)
            .setMaxValues(roles.size())
            .addOptions(roles.stream()
                .filter(role -> !role.isPublicRole())
                .filter(role -> !role.getTags().isBot())
                .map(RoleSelectCommand::roleToSelection)
                .toList());
    }

    @NonNull
    private static SelectOption roleToSelection(@NonNull final Role role) {
        final var roleIcon = role.getIcon();

        if (roleIcon == null || !roleIcon.isEmoji() || !roleIcon.isEmoji()) {
            return SelectOption.of(role.getName(), role.getId());
        } else {
            return SelectOption.of(role.getName(), role.getId())
                .withEmoji(Emoji.fromUnicode(Objects.requireNonNull(roleIcon.getEmoji())));
        }
    }

    public void onModalInteraction(final ModalInteractionContext context) {
        final var event = context.getEvent();
        if (!event.isFromGuild()) return;
        final var menu = COMPONENT_LISTENER.createMenu(new String[]{MenuType.IN_CREATION.toString()},
            Component.Lifespan.TEMPORARY,
            // Parse the isDropdown
            context.getArguments().get(0));

        addMenuOptions(event, menu, "Select the roles to display", 1);

        final var titleOption = event.getValue("title");
        final var descriptionOption = event.getValue("description");

        final var title = titleOption == null ? null : titleOption.getAsString();
        final var description = descriptionOption == null ? null : descriptionOption.getAsString();

        event.reply(new MessageBuilder()
                .setEmbeds(
                    new EmbedBuilder()
                        .setTitle(title)
                        .setColor(COLOUR)
                        .setDescription(description)
                        .build()
                )
                .setActionRows(ActionRow.of(menu.build()))
                .build())
            .setEphemeral(true)
            .queue();
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

        final boolean isDropdown = Boolean.parseBoolean(context.getArguments().get(0));

        switch (MenuType.valueOf(context.getItemComponentArguments().get(0))) {
            case IN_CREATION -> {
                if (isDropdown) {
                    handleDropdownCreation(context, selectedRoles);
                } else {
                    handleButtonOption(context, selectedRoles);
                }
            }
            case ROLE_PANEL -> handleRoleSelection(context, selectedRoles, guild);
        }
    }

    private static void handleButtonOption(final @NonNull SelectMenuInteractionContext context, final List<Role> selectedRoles) {
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
        /* TODO what's the actual limit?
        if (rows.size() > 5) {
            context.getEvent().deferReply(true).setContent("Too many roles selected!");
            return;
        }
         */

        final var component = new Component(COMPONENT_LISTENER.getName(), id, List.of(), Component.Lifespan.PERMANENT);
        COMPONENT_LISTENER.insertComponent(component);

        context.getEvent().getChannel()
            .sendMessageEmbeds(context.getEvent().getMessage().getEmbeds().get(0))
            .setActionRows(rows)
            .queue();

        context.getEvent().reply("Message created and sent successfully!").setEphemeral(true).queue();
    }

    private static Button createButtonForRole(final String id, final Role role) {
        final var icon = role.getIcon();
        final var bId = Component.createIdWithArguments(id, role.getId());
        if (icon != null && icon.isEmoji()) {
            return Button.of(ButtonStyle.PRIMARY, bId, role.getName(), Emoji.fromUnicode(icon.getEmoji()));
        }
        return Button.primary(bId, role.getName());
    }

    private static void handleDropdownCreation(final @NonNull SelectMenuInteractionContext context, final List<Role> selectedRoles) {
        SelectMenu.Builder menu = COMPONENT_LISTENER.createMenu(new String[] {MenuType.ROLE_PANEL.toString()}, Component.Lifespan.PERMANENT, String.valueOf(true))
            .setPlaceholder("Select your roles")
            .setMaxValues(selectedRoles.size())
            .setMinValues(0);

        selectedRoles.forEach(role -> menu.addOption(role.getName(), role.getId()));

        context.getEvent().getChannel()
            .sendMessageEmbeds(context.getEvent().getMessage().getEmbeds().get(0))
            .setActionRow(menu.build())
            .queue();

        context.getEvent().reply("Message created and sent successfully!").setEphemeral(true).queue();
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

    enum MenuType {
        IN_CREATION,
        ROLE_PANEL
    }
}
