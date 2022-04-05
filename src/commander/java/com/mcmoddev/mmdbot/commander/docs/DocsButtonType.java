package com.mcmoddev.mmdbot.commander.docs;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import net.dv8tion.jda.api.MessageBuilder;

import java.util.UUID;
import java.util.function.BiConsumer;

public enum DocsButtonType {

    EXPAND("\uD83D\uDD3D Expand", (context, command) -> {
        final var data = DocsButtonData.fromArguments(context.getArguments());
        if (notOwner(data.buttonOwner(), context)) return;
        context.getEvent()
            .deferEdit()
            .queue(hook -> command.query(
                data.query(),
                context.getUser().getIdLong(),
                context.getComponentId(),
                m -> context.getEvent().getMessage().editMessage(m)
                    .map(msg -> {
                        context.updateArguments(data.withShortDescription(false).toArguments());
                        return msg;
                    }),
                false,
                data.omitTags(),
                () -> hook.editOriginal("Query has no result.")
                    .setActionRow(DismissListener.createDismissButton())
                    .queue())
            );
    }),
    COLLAPSE("\uD83D\uDD3C Collapse", (context, command) -> {
        final var data = DocsButtonData.fromArguments(context.getArguments());
        if (notOwner(data.buttonOwner(), context)) return;
        context.getEvent()
            .deferEdit()
            .queue(hook -> command.query(
                data.query(),
                context.getUser().getIdLong(),
                context.getComponentId(),
                m -> context.getEvent().getMessage().editMessage(m)
                    .map(msg -> {
                        context.updateArguments(data.withShortDescription(true).toArguments());
                        return msg;
                    }),
                true,
                data.omitTags(),
                () -> hook.editOriginal("Query has no result.")
                    .setActionRow(DismissListener.createDismissButton())
                    .queue())
            );
    }),
    ADD_TAGS("\uD83D\uDDA8️ Show Tags", (context, command) -> {
        final var data = DocsButtonData.fromArguments(context.getArguments());
        if (notOwner(data.buttonOwner(), context)) return;
        context.getEvent()
            .deferEdit()
            .queue(hook -> command.query(
                data.query(),
                context.getUser().getIdLong(),
                context.getComponentId(),
                m -> context.getEvent().getMessage().editMessage(m)
                    .map(msg -> {
                        context.updateArguments(data.withOmitTags(false).toArguments());
                        return msg;
                    }),
                data.shortDescription(),
                false,
                () -> hook.editOriginal("Query has no result.")
                    .setActionRow(DismissListener.createDismissButton())
                    .queue())
            );
    }),
    REMOVE_TAGS("✂️ Hide Tags", (context, command) -> {
        final var data = DocsButtonData.fromArguments(context.getArguments());
        if (notOwner(data.buttonOwner(), context)) return;
        context.getEvent()
            .deferEdit()
            .queue(hook -> command.query(
                data.query(),
                context.getUser().getIdLong(),
                context.getComponentId(),
                m -> context.getEvent().getMessage().editMessage(m)
                    .map(msg -> {
                        context.updateArguments(data.withOmitTags(true).toArguments());
                        return msg;
                    }),
                data.shortDescription(),
                true,
                () -> hook.editOriginal("Query has no result.")
                    .setActionRow(DismissListener.createDismissButton())
                    .queue())
            );
    }),
    MULTIPLE_RESULTS("Multiple Results", (context, command) -> {
        final var data = MultipleResultsButtonData.fromArguments(context.getArguments());
        if (notOwner(data.userId(), context)) return;
        final var buttonIndex = Integer.parseInt(context.getItemComponentArguments().get(1));
        final var query = data.queries().get(buttonIndex);
        context.getEvent().deferEdit().queue();

        command.query(
            query,
            context.getUser().getIdLong(),
            context.getComponentId(),
            m -> context.getEvent().getMessage().editMessage(m)
                .content("\u200b") // use a ZWSP to "remove" the content
                .map(msg -> {
                    final var compData = new DocsButtonData(context.getUser().getIdLong(), query, true, false);
                    final var component = new Component(command.componentListener.getName(), UUID.randomUUID(), compData.toArguments(), Component.Lifespan.TEMPORARY);
                    command.componentListener.insertComponent(component);
                    return msg;
                }),
            true,
            false,
            () -> context.getEvent().getHook()
                    .editOriginal("Query has no result.")
                    .setActionRow(DismissListener.createDismissButton())
                .queue());
    });

    public final String name;
    private final BiConsumer<ButtonInteractionContext, DocsCommand> handler;

    DocsButtonType(final String name, final BiConsumer<ButtonInteractionContext, DocsCommand> handler) {
        this.name = name;
        this.handler = handler;
    }

    public void handleClick(final ButtonInteractionContext context, final DocsCommand command) {
        handler.accept(context, command);
    }

    private static boolean notOwner(final long owner, final ButtonInteractionContext context) {
        final var res = owner == context.getUser().getIdLong();
        if (!res) context.getEvent().deferEdit().queue();
        return !res;
    }
}
