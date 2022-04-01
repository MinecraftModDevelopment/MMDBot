package com.mcmoddev.mmdbot.core.commands.paginate;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for paginating messages, using {@link Component Components}. <br>
 * There should be only one {@link Paginator} instance per feature, which should be reused.
 */
public interface Paginator {
    Emoji NEXT_EMOJI = Emoji.fromUnicode("▶️");
    Emoji PREVIOUS_EMOJI = Emoji.fromUnicode("◀️");

    String FORWARD_BUTTON_ID = "next";
    String BACKWARD_BUTTON_ID = "prev";

    /**
     * Gets the component listener for handling the pagination. <br>
     * <strong>The listener has to be registered to a {@link com.mcmoddev.mmdbot.core.commands.component.ComponentManager}
     * before the paginator can be used.</strong>
     *
     * @return the component listener.
     */
    ComponentListener getListener();

    /**
     * The lifespan of the buttons created by the paginator.
     *
     * @return the lifespan of the buttons created by the paginator
     */
    default Component.Lifespan getLifespan() {
        return Component.Lifespan.TEMPORARY;
    }

    /**
     * How many items should be sent per individual page.
     *
     * @return how many items should be sent per individual page
     */
    default int getItemsPerPage() {
        return 25;
    }

    /**
     * If messages sent by the paginator should be dismissible. <br>
     * The message is only dismissible by the user that triggered the event which made the message to be sent.
     *
     * @return if message sent by the paginator should be dismissible
     */
    default boolean isDismissible() {
        return true;
    }

    /**
     * If the "scroll" buttons can only be used by the owner of the sent message. <br>
     * This is mainly used for commands that display user-specific information.
     *
     * @return if the scroll buttons are owner only
     */
    default boolean areButtonsOwnerOnly() {
        return false;
    }

    /**
     * Given the index of the start of the message, get the next {@link #getItemsPerPage() items}. <br>
     * Before being sent, the message will have the pagination buttons added.
     *
     * @param startingIndex the index of the first item in the list.
     * @param maximum       the maximum amount of items to be displayed
     * @param arguments     the arguments of the button
     * @return a built message that can be sent.
     */
    @Nonnull
    MessageBuilder getMessage(int startingIndex, int maximum, final List<String> arguments);

    /**
     * Create the row of Component interaction buttons.
     * <p>
     * Currently, this just creates a left and right arrow, and if the message {@link #isDismissible()} a dismiss button.
     * Left arrow scrolls back a page. Right arrow scrolls forward a page.
     *
     * @param id          the component ID of the buttons
     * @param start       the index of the item at the start of the current page.
     * @param maximum     the maximum amount of items
     * @param buttonOwner the ID of the owner of the button. Can be {@code null} unless {@link #areButtonsOwnerOnly() buttons are owner-only}.
     * @return A row of buttons to go back and forth by one page.
     */
    ActionRow createScrollButtons(String id, int start, int maximum, @Nullable Long buttonOwner);

    /**
     * Create a {@link Message} which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param startingIndex the index of the first item to display
     * @param maximum       the maximum of items
     * @param messageOwner  the ID of the "owner" of the message (mainly used for commands). Can be {@code null} unless {@link #areButtonsOwnerOnly() buttons are owner-only}.
     * @param args          arguments the arguments that will be saved in the database, bound to the button's component ID
     * @return the ReplyAction
     */
    Message createPaginatedMessage(final int startingIndex, final int maximum, @Nullable final Long messageOwner, final List<String> args);

    /**
     * Create a {@link Message} which, if the number of items requires, also contains buttons for scrolling.
     *
     * @param startingIndex the index of the first item to display
     * @param maximum       the maximum of items
     * @param messageOwner  the ID of the "owner" of the message (mainly used for commands). Can be {@code null} unless {@link #areButtonsOwnerOnly() buttons are owner-only}.
     * @param args          arguments the arguments that will be saved in the database, bound to the button's component ID
     * @return the ReplyAction
     */
    default Message createPaginatedMessage(final int startingIndex, final int maximum, @Nullable final Long messageOwner, final String... args) {
        return createPaginatedMessage(startingIndex, maximum, messageOwner, Arrays.asList(args));
    }

    /**
     * Creates a {@link Paginator} builder.
     *
     * @param componentListener the builder of the component listener that the paginator will use
     * @return the builder
     */
    static PaginatorBuilder builder(final ComponentListener.Builder componentListener) {
        return new PaginatorBuilder(componentListener);
    }

    @FunctionalInterface
    interface MessageGetter {
        @Nonnull
        MessageBuilder getMessage(int startingIndex, int maximum, final List<String> arguments);
    }
}
