package com.mcmoddev.mmdbot.commander.eventlistener;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.util.MessageUtilities;
import lombok.NonNull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.MarkdownUtil;

import java.awt.Color;

public final class ReferencingListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || TheCommander.getInstance() == null ||
            !TheCommander.getInstance().getGeneralConfig().features().isReferencingEnabled()) {
            return;
        }
        final var originalMsg = event.getMessage();
        if (originalMsg.getMessageReference() != null && isStringReference(originalMsg.getContentRaw())) {
            final var referencedMessage = originalMsg.getMessageReference().getMessage();
            if (referencedMessage != null) {
                event.getChannel().sendMessageEmbeds(reference(referencedMessage, event.getMember())).queue();
                originalMsg.delete().reason("Quote successful").queue();
                return;
            }
        }

        final String[] msg = originalMsg.getContentRaw().split(" ");
        if (msg.length < 1) {
            return;
        }

        final var matcher = MessageUtilities.MESSAGE_LINK_PATTERN.matcher(msg[0]);
        if (matcher.find()) {
            try {
                final var m = TheCommander.getInstance().getMessageByLink(msg[0]);
                if (m != null) {
                    m.queue(message -> {
                        event.getChannel().sendMessageEmbeds(reference(message, event.getMember())).queue();
                        if (msg.length == 1) {
                            originalMsg.delete().reason("Quote successful").queue();
                        }
                    });
                }
            } catch (MessageUtilities.MessageLinkException e) {
                // Do nothing
            }
        }
    }

    private static boolean isStringReference(@NonNull final String string) {
        return string.equals(".") || string.equals("^") || string.isBlank();
    }

    public static MessageEmbed reference(@NonNull final Message message, final Member quoter) {
        final var hasAuthor = !message.isWebhookMessage();
        final var msgLink = message.getJumpUrl();
        final var embed = new EmbedBuilder().setTimestamp(message.getTimeCreated())
            .setColor(Color.DARK_GRAY);
        if (hasAuthor) {
            embed.setAuthor(message.getAuthor().getAsTag(), msgLink, message.getAuthor().getEffectiveAvatarUrl());
        }
        if (!message.getContentRaw().isBlank()) {
            embed.appendDescription(MarkdownUtil.maskedLink("Reference ➤ ", msgLink))
                .appendDescription(message.getContentRaw());
        } else {
            embed.appendDescription(MarkdownUtil.maskedLink("Jump to referenced message.", msgLink));
        }
        if (quoter.getIdLong() != message.getAuthor().getIdLong()) {
            embed.setFooter(quoter.getUser().getAsTag() + " referenced", quoter.getEffectiveAvatarUrl());
        }
        if (!message.getAttachments().isEmpty()) {
            embed.setImage(message.getAttachments().get(0).getUrl());
        }
        return embed.build();
    }

}
