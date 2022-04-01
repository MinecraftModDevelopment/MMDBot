package com.mcmoddev.mmdbot.core.commands.paginate;

import com.google.common.collect.Lists;
import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class PaginatorImpl implements Paginator {
    private final ComponentListener listener;
    private final Component.Lifespan lifespan;
    private final int itemsPerPage;
    private final boolean dismissible;
    private final boolean buttonsOwnerOnly;
    private final MessageGetter embedGetter;

    PaginatorImpl(final ComponentListener.Builder listener, @Nullable final Consumer<? super ButtonInteractionContext> buttonInteractionHandler, final Component.Lifespan lifespan, final int itemsPerPage, final boolean dismissible, final boolean buttonsOwnerOnly, final MessageGetter embedGetter) {
        this.embedGetter = embedGetter;
        this.listener = listener
            .onButtonInteraction(buttonInteractionHandler == null ? this::onButtonInteraction : buttonInteractionHandler)
            .build();
        this.lifespan = lifespan;
        this.itemsPerPage = itemsPerPage;
        this.dismissible = dismissible;
        this.buttonsOwnerOnly = buttonsOwnerOnly;
    }

    /**
     * The default button interaction handler.
     */
    public void onButtonInteraction(final ButtonInteractionContext context) {
        final var owner = context.getItemComponentArguments().size() > 1 ? Long.parseLong(context.getItemComponentArguments().get(1)) : null;
        final var event = context.getEvent();
        final var interaction = event.getMessage().getInteraction();
        if (areButtonsOwnerOnly()) {
            if (owner != null) {
                if (owner != event.getUser().getIdLong()) {
                    event.deferEdit().queue();
                    return;
                }
            } else if (interaction != null && interaction.getUser().getIdLong() != event.getUser().getIdLong()) {
                event.deferEdit().queue();
                return;
            }
        }
        final int current = context.getArgument(0, () -> 0, Integer::parseInt);
        final int maximum = context.getArgument(1, () -> 0, Integer::parseInt);
        final List<String> newArgs = context.getArguments().size() == 2 ? List.of() : context.getArguments().subList(2, context.getArguments().size());

        // If it has action rows already, don't delete them
        final var oldActionRowsSize = event.getMessage().getActionRows().size();
        final var oldActionRows = oldActionRowsSize < 2 ? new ArrayList<ActionRow>() :
            new ArrayList<>(event.getMessage().getActionRows().subList(1, oldActionRowsSize));

        final var buttonId = context.getComponentId().toString();

        switch (context.getItemComponentArguments().get(0)) {
            case FORWARD_BUTTON_ID -> {
                final var start = current + itemsPerPage;

                oldActionRows.add(0, createScrollButtons(buttonId, start, maximum, owner));
                event.editMessage(getMessage(start, maximum, newArgs).build())
                    .setActionRows(oldActionRows)
                    .queue();

                // Argument 0 == current index
                context.updateArgument(0, String.valueOf(start));
            }
            case BACKWARD_BUTTON_ID -> {
                final var start = current - itemsPerPage;

                oldActionRows.add(0, createScrollButtons(buttonId, start, maximum, owner));
                event.editMessage(getMessage(start, maximum, newArgs).build())
                    .setActionRows(oldActionRows)
                    .queue();

                // Argument 0 == current index
                context.updateArgument(0, String.valueOf(start));
            }
        }
    }

    @NotNull
    @Override
    public MessageBuilder getMessage(final int startingIndex, final int maximum, final List<String> arguments) {
        return embedGetter.getMessage(startingIndex, maximum, arguments);
    }

    @Override
    public ComponentListener getListener() {
        return listener;
    }

    @Override
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    @Override
    public Component.Lifespan getLifespan() {
        return lifespan;
    }

    @Override
    public boolean areButtonsOwnerOnly() {
        return buttonsOwnerOnly;
    }

    @Override
    public boolean isDismissible() {
        return dismissible;
    }

    @Override
    public ActionRow createScrollButtons(final String id, final int start, final int maximum, @Nullable final Long buttonOwner) {
        Button backward = Button.primary(Component.createIdWithArguments(id, areButtonsOwnerOnly() ? new Object[]{BACKWARD_BUTTON_ID, buttonOwner} : new Object[]{BACKWARD_BUTTON_ID}), PREVIOUS_EMOJI).asDisabled();
        Button forward = Button.primary(Component.createIdWithArguments(id, areButtonsOwnerOnly() ? new Object[]{FORWARD_BUTTON_ID, buttonOwner} : new Object[]{FORWARD_BUTTON_ID}), NEXT_EMOJI).asDisabled();

        if (start != 0) {
            backward = backward.asEnabled();
        }

        if (start + getItemsPerPage() < maximum) {
            forward = forward.asEnabled();
        }

        if (isDismissible()) {
            return ActionRow.of(backward, buttonOwner == null ? DismissListener.createDismissButton() : DismissListener.createDismissButton(buttonOwner), forward);
        }

        return ActionRow.of(backward, forward);
    }

    @Override
    public Message createPaginatedMessage(final int startingIndex, final int maximum, final @Nullable Long messageOwner, final List<String> args) {
        final var id = UUID.randomUUID();
        final var argsList = Lists.newArrayList(args);
        final var message = new MessageBuilderWidener(getMessage(startingIndex, maximum, argsList));
        final var startStr = String.valueOf(startingIndex);
        final var maxStr = String.valueOf(maximum);
        if (argsList.size() == 0) {
            argsList.add(startStr);
            argsList.add(maxStr);
        } else {
            argsList.add(0, startStr);
            argsList.add(1, maxStr);
        }
        final var component = new Component(getListener().getName(), id, argsList, Component.Lifespan.TEMPORARY);
        getListener().insertComponent(component);
        message.addActionRow(createScrollButtons(id.toString(), startingIndex, maximum, messageOwner));
        return message.build();
    }

    protected static class MessageBuilderWidener extends MessageBuilder {
        public MessageBuilderWidener(final MessageBuilder builder) {
            super(builder);
        }
        public void addActionRow(final ActionRow actionRow) {
            components.add(actionRow);
        }
    }
}
