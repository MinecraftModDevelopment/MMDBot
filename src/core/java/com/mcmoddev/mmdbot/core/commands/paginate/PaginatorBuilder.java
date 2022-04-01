package com.mcmoddev.mmdbot.core.commands.paginate;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.context.ButtonInteractionContext;
import lombok.NonNull;
import net.dv8tion.jda.api.MessageBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PaginatorBuilder {
    private final ComponentListener.Builder componentListener;
    private Consumer<? super ButtonInteractionContext> buttonInteractionHandler;
    private Component.Lifespan lifespan = Component.Lifespan.TEMPORARY;
    private int itemsPerPage = 25;
    private boolean dismissible = true;
    private boolean buttonsOwnerOnly;
    private Paginator.MessageGetter messageGetter = (startingIndex, maximum, arguments) -> new MessageBuilder("Unknown page.");

    PaginatorBuilder(final ComponentListener.Builder componentListener) {
        this.componentListener = componentListener;
    }

    /**
     * Sets the lifespan of the pagination buttons.
     *
     * @param lifespan the lifespan of the pagination buttons
     * @return the builder instance
     */
    public PaginatorBuilder lifespan(@NonNull final Component.Lifespan lifespan) {
        this.lifespan = lifespan;
        return this;
    }

    /**
     * Sets the amount of items a page has.
     *
     * @param itemsPerPage the amount of items a page has
     * @return the builder instance
     */
    public PaginatorBuilder itemsPerPage(final int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
        return this;
    }

    /**
     * Sets if the paginated messages are dismissible.
     *
     * @param dismissible if the paginated messages are dismissible
     * @return the builder instance
     */
    public PaginatorBuilder dismissible(final boolean dismissible) {
        this.dismissible = dismissible;
        return this;
    }

    /**
     * Sets if the "scroll" buttons can only be used by the owner of the sent message. <br>
     * This is mainly used for commands that display user-specific information.
     *
     * @param buttonsOwnerOnly if the "scroll" buttons can only be used by their owner
     * @return the builder instance
     */
    public PaginatorBuilder buttonsOwnerOnly(final boolean buttonsOwnerOnly) {
        this.buttonsOwnerOnly = buttonsOwnerOnly;
        return this;
    }

    /**
     * Sets the getter used for building the message "pages".
     *
     * @param messageGetter the getter used for building the message "pages"
     * @return the builder instance
     */
    public PaginatorBuilder message(@NonNull final Paginator.MessageGetter messageGetter) {
        this.messageGetter = messageGetter;
        return this;
    }

    /**
     * Sets the handler that will paginate messages. <br>
     * If {@code null} or not set, the handler will be the default one. <br>
     * Most implementations don't need to mess with it.
     *
     * @param buttonInteractionHandler the handler that will paginate messages
     * @return the builder instance
     */
    public PaginatorBuilder buttonInteractionHandler(@Nullable final Consumer<? super ButtonInteractionContext> buttonInteractionHandler) {
        this.buttonInteractionHandler = buttonInteractionHandler;
        return this;
    }

    /**
     * Builds the {@link Paginator}.
     *
     * @return the built paginator
     */
    public Paginator build() {
        return new PaginatorImpl(componentListener, buttonInteractionHandler, lifespan, itemsPerPage, dismissible, buttonsOwnerOnly, messageGetter);
    }
}