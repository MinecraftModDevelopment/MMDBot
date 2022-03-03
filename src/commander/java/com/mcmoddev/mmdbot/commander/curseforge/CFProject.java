package com.mcmoddev.mmdbot.commander.curseforge;

import com.mcmoddev.mmdbot.commander.TheCommander;
import io.github.matyrobbrt.curseforgeapi.request.AsyncRequest;
import io.github.matyrobbrt.curseforgeapi.request.Response;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public record CFProject(int projectId, List<Long> channels, AtomicInteger lastFoundFile) implements Runnable {
    private static final Collection<Message.MentionType> ALLOWED_MENTIONS = List.of();

    @Override
    public void run() {
        final var $$1 = TheCommander.getInstance().getCurseForgeManager().orElseThrow();
        final var api = $$1.api();
        final var allProjects = $$1.projects();

        try {
            api.getAsyncHelper().getMod(projectId)
                .flatMap(r ->
                    r.flatMap(m -> {
                        if (m.latestFilesIndexes().isEmpty() || (m.latestFilesIndexes().get(0).fileId() <= lastFoundFile.get())) {
                            return Response.empty(r.getStatusCode());
                        }
                        return r;
                    }).mapOrElseWithException(m -> {
                        final var latestFile = m.latestFilesIndexes().get(0).fileId();
                        final var toRet = CFUtils.createFileEmbed(m, latestFile);
                        lastFoundFile.set(latestFile);
                        allProjects.save();
                        return toRet;
                    }, AsyncRequest::empty)
                )
                .map(e -> {
                    if (channels.isEmpty()) {
                        return null;
                    }
                    return RestAction.allOf(channels.stream().map(id -> TheCommander.getJDA().getGuildChannelById(id))
                        .filter(c -> c instanceof MessageChannel)
                        .map(MessageChannel.class::cast)
                        .map(c -> c.sendMessageEmbeds(e.build()).allowedMentions(ALLOWED_MENTIONS))
                        .toList());
                })
                .queue(actions -> {
                    if (actions != null) {
                        actions.queue();
                    }
                });
        } catch (Exception e) {
            TheCommander.LOGGER.error("Exception while trying to send CurseForge update message!", e);
        }
    }
}
