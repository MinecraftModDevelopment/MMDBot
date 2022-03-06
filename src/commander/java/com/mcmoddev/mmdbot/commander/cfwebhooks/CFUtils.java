package com.mcmoddev.mmdbot.commander.cfwebhooks;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.core.util.Utils;
import io.github.matyrobbrt.curseforgeapi.request.AsyncRequest;
import io.github.matyrobbrt.curseforgeapi.request.helper.AsyncRequestHelper;
import io.github.matyrobbrt.curseforgeapi.schemas.mod.Mod;
import io.github.matyrobbrt.curseforgeapi.util.CurseForgeException;
import io.github.matyrobbrt.curseforgeapi.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Webhook;
import okhttp3.OkHttpClient;
import org.jsoup.Jsoup;

import java.awt.Color;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.lineSeparator;

@UtilityClass
public class CFUtils {

    public static final ScheduledExecutorService WEBHOOKS_EXECUTOR = Executors.newScheduledThreadPool(1, r ->
        Utils.setThreadDaemon(new Thread(r, "CurseForgeWebhooks"), true));
    public static final OkHttpClient WEBHOOKS_HTTP_CLIENT = new OkHttpClient();
    public static final Long2ObjectMap<JDAWebhookClient> WEBHOOKS = new Long2ObjectOpenHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            WEBHOOKS.values().forEach(JDAWebhookClient::close), "CurseForgeWebhookCloser"));
    }

    public static AsyncRequest<EmbedBuilder> createWebhookFileEmbed(Mod mod, int fileId) throws CurseForgeException {
        return getAsyncApiHelper().getModFile(mod.id(), fileId)
            .and(getAsyncApiHelper().getModFileChangelog(mod.id(), fileId))
            .map(Pair::mapResponses)
            .map(Optional::orElseThrow)
            .map(p -> p.map((file, changelog) -> {
                    final var embed = new EmbedBuilder()
                        .setTimestamp(Instant.parse(file.fileDate()))
                        .setTitle(mod.name(), mod.links().websiteUrl() /* The mod CF link */)
                        .setColor(Color.DARK_GRAY)
                        .setThumbnail(mod.logo().thumbnailUrl())
                        .appendDescription("New file detected for the CurseForge project `%s`".formatted(mod.name()))
                        .appendDescription(lineSeparator())
                        .appendDescription(lineSeparator())
                        .appendDescription("Release Type: `%s`".formatted(file.releaseType()))
                        .appendDescription(lineSeparator())
                        .appendDescription("File Name: `%s`".formatted(file.fileName()))
                        .appendDescription(lineSeparator())
                        .appendDescription("Game Versions: `%s`".formatted(String.join(", ", file.gameVersions())))
                        .appendDescription(lineSeparator())
                        .appendDescription("Download URL: [Download](%s)".formatted(file.downloadUrl()))
                        .appendDescription(lineSeparator())
                        .appendDescription(lineSeparator());

                    try {
                        embed.appendDescription("""
                            Changelog:
                            ```
                            %s
                            ```""".formatted(Jsoup.parse(changelog).text()));
                    } catch (IllegalArgumentException e) {
                        embed.appendDescription("Changelog: *Too big to be displayed*");
                    }
                    return embed;
                })
            );
    }

    public static final String WEBHOOK_SUFFIX = "[CF]";

    public static Webhook getOrCreateWebhook(long channelId) {
        final var channel = TheCommander.getJDA().getChannelById(BaseGuildMessageChannel.class, channelId);
        final var alreadyExisted = Objects.requireNonNull(channel).retrieveWebhooks()
            .complete()
            .stream()
            .filter(w -> w.getName().endsWith(WEBHOOK_SUFFIX))
            .findAny();
        if (alreadyExisted.isPresent()) {
            return alreadyExisted.get();
        }
        final var webhook = channel.createWebhook("CurseForgeWebhooks %s".formatted(WEBHOOK_SUFFIX)).complete();
        try {
            final var icon = Icon.from(Objects.requireNonNull(TheCommander.class.getResourceAsStream("/commander/cf_logo.png")));
            webhook.getManager().setAvatar(icon).complete();
        } catch (IOException e) {
            TheCommander.LOGGER.error("Exception while trying to set icon for CurseForge webhook in channel {}: {}", channelId, e);
        }
        return webhook;
    }

    public static JDAWebhookClient getWebhookClient(long channelId) {
        return WEBHOOKS.computeIfAbsent(channelId, k ->
            WebhookClientBuilder.fromJDA(getOrCreateWebhook(channelId))
                .setExecutorService(WEBHOOKS_EXECUTOR)
                .setHttpClient(WEBHOOKS_HTTP_CLIENT)
                .setAllowedMentions(AllowedMentions.none())
                .buildJDA());
    }

    private static AsyncRequestHelper getAsyncApiHelper() {
        return TheCommander.getInstance().getCurseForgeManager().orElseThrow().api().getAsyncHelper();
    }

}
