package com.mcmoddev.mmdbot.modules.commands.general;

import com.jagrosh.jdautilities.command.SlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper for Slash Commands which require a paginated embed.
 * It handles the buttons and interactions for you.
 *
 * To use this, the developer needs to:
 *  - implement {@link #getEmbed(int)} as per the javadoc.
 *  - implement {@link ButtonListener} in the child class, along with the {@link ButtonListener#getButtonID()} method.
 *  - register their implementation of {@link ButtonListener} to the {@link com.jagrosh.jdautilities.command.CommandClient}.
 *  - call {@link #updateMaximum(int)} as required - usually once per invocation
 *  - call {@link #sendPaginatedMessage(SlashCommandEvent)} in the execute method when a paginated embed is wanted.
 *
 * @author Curle
 */
public abstract class PaginatedCommand extends SlashCommand {

    // How many items should be sent per individual page. Defaults to the maximum field count for an Embed, 25.
    protected int items_per_page = 25;
    // The maxmimum number of items in the list. Update with #updateMaximum
    protected int maximum = 0;

    public PaginatedCommand(String name, String help, boolean guildOnly, List<OptionData> options, int items) {
        super();
        this.name = name;
        this.help = help;

        this.guildOnly = guildOnly;

        items_per_page = items;

        this.options = options;
    }

    /**
     * Given the index of the start of the embed, get the next ITEMS_PER_PAGE items.
     * This is where the implementation of the paginated command steps in.
     * @param startingIndex the index of the first item in the list.
     * @return an unbuilt embed that can be sent.
     */
    protected abstract EmbedBuilder getEmbed(int startingIndex);

    /**
     * Set a new maximum index into the paginated list.
     * Updates the point at which buttons are created in new queries.
     */
    protected void updateMaximum(int newMaxmimum) {
        maximum = newMaxmimum;
    }

    /**
     * Create and queue a ReplyAction which, if the number of items requires, also contains buttons for scrolling.
     * @param event the active SlashCommandEvent.
     */
    protected void sendPaginatedMessage(SlashCommandEvent event) {
        ReplyAction reply = event.replyEmbeds(getEmbed(0).build()).setEphemeral(true);
        Component[] buttons = createScrollButtons(0);
        if (buttons.length > 0)
            reply.addActionRow(buttons);

        reply.queue();
    }

    /**
     * Create the row of Component interaction buttons.
     * <p>
     * Currently, this just creates a left and right arrow.
     * Left arrow scrolls back a page. Right arrow scrolls forward a page.
     *
     * @param start The quote number at the start of the current page.
     * @return A row of buttons to go back and forth by one page.
     */
    private Component[] createScrollButtons(int start) {
        List<Component> components = new ArrayList<>();
        if (start != 0) {
            components.add(Button.primary(getName() + "-" + start + "-prev",
                Emoji.fromUnicode("◀️")));
        }
        if (start + items_per_page < maximum) {
            components.add(Button.primary(getName() + "-" + start + "-next",
                Emoji.fromUnicode("▶️")));
        }
        return components.toArray(new Component[0]);
    }


    /**
     * Listens for interactions with the scroll buttons on the paginated message.
     * Extend and implement as a child class of the Paginated Message.
     *
     * Implement the {@link #getButtonID()} function in any way you like.
     * Make sure that this listener is registered to the {@link com.jagrosh.jdautilities.command.CommandClient}.
     */
    public abstract class ButtonListener extends ListenerAdapter {

        public abstract String getButtonID();

        @Override
        public void onButtonClick(@NotNull final ButtonClickEvent event) {
            var button = event.getButton();
            if (button == null || button.getId() == null) {
                return;
            }

            String[] idParts = button.getId().split("-");
            if (idParts.length != 3) {
                return;
            }

            if (!idParts[0].equals(getButtonID())) {
                return;
            }

            int current = Integer.parseInt(idParts[1]);

            if (idParts[2].equals("next")) {
                event
                    .editMessageEmbeds(getEmbed(current + items_per_page).build())
                    .setActionRow(createScrollButtons(current + items_per_page))
                    .queue();
            } else {
                if (idParts[2].equals("prev")) {
                    event
                        .editMessageEmbeds(getEmbed(current - items_per_page).build())
                        .setActionRow(createScrollButtons(current - items_per_page))
                        .queue();
                }
            }
        }
    }
}
