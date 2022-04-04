package com.mcmoddev.mmdbot.commander.docs;

import com.mcmoddev.mmdbot.core.commands.component.Component;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import de.ialistannen.javadocapi.model.JavadocElement;
import de.ialistannen.javadocapi.rendering.LinkResolveStrategy;
import de.ialistannen.javadocapi.rendering.MarkdownCommentRenderer;
import de.ialistannen.javadocapi.storage.ElementLoader;
import de.ialistannen.javadocapi.util.BaseUrlElementLoader;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Function;

public class NormalDocsSender implements DocsSender {
    @Override
    public void replyWithResult(final Function<Message, RestAction<Message>> replier, final ElementLoader.LoadResult<JavadocElement> loadResult, final boolean shortDescription, final boolean omitTags, final Duration queryDuration, final LinkResolveStrategy linkResolveStrategy, final long userId, final UUID buttonId) {
        final var embed = new DocsEmbed(
            new MarkdownCommentRenderer(linkResolveStrategy),
            loadResult.getResult(),
            ((BaseUrlElementLoader) loadResult.getLoader()).getBaseUrl()
        )
            .addColor()
            .addIcon(linkResolveStrategy)
            .addDeclaration()
            .addFooter(loadResult.getLoader().toString(), queryDuration);

        if (shortDescription) {
            embed.addShortDescription();
        } else {
            embed.addLongDescription();
        }
        if (!omitTags) {
            embed.addTags();
        }

        final var btnId = buttonId.toString();
        final var buttons = new ArrayList<Button>();
        if (shortDescription) {
            buttons.add(button(btnId, DocsButtonType.EXPAND, ButtonStyle.SECONDARY));
        } else {
            buttons.add(button(btnId, DocsButtonType.COLLAPSE, ButtonStyle.SECONDARY));
        }
        if (omitTags) {
            buttons.add(button(btnId, DocsButtonType.ADD_TAGS, ButtonStyle.SECONDARY));
        } else {
            buttons.add(button(btnId, DocsButtonType.REMOVE_TAGS, ButtonStyle.SECONDARY));
        }
        buttons.add(DismissListener.createDismissButton(userId));

        replier.apply(new MessageBuilder(embed.build())
            .setActionRows(ActionRow.of(buttons))
            .build())
            .queue();
    }

    public static Button button(final String id, final DocsButtonType type, ButtonStyle style) {
        return Button.of(style, Component.createIdWithArguments(id, type.toString()), type.name);
    }
}
