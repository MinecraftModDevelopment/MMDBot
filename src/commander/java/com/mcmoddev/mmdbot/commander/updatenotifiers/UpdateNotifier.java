package com.mcmoddev.mmdbot.commander.updatenotifiers;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import com.mcmoddev.mmdbot.commander.util.dao.UpdateNotifiersDAO;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Builder;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Webhook;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import static com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers.LOGGER;

/**
 * The base class of update notifiers.
 *
 * @param <T> the type of the data this notifier uses for storing latest versions
 * @author matyrobbrt
 */
@ParametersAreNonnullByDefault
public abstract class UpdateNotifier<T> implements Runnable {
    protected final NotifierConfiguration<T> configuration;
    protected final Marker loggingMarker;
    private T latest;
    private boolean pickedUpFromDB;

    protected UpdateNotifier(final NotifierConfiguration<T> configuration) {
        this.configuration = configuration;
        this.loggingMarker = MarkerFactory.getMarker(configuration.name);
    }

    /**
     * Queries the latest version of the listened project. <br> <br>
     * This method will be called every time this notifier {@link #run() runs},
     * so you can use it for updating versions stored externally as well.
     *
     * @return the latest version, or if one was not found, {@code null}
     * @throws IOException if an exception occurred querying the version
     */
    @Nullable
    protected abstract T queryLatest() throws IOException;

    /**
     * Gets the embed that will be sent to the channels this notifier is configured to inform.
     *
     * @param oldVersion the old version, or {@code null} if one has not been previously found
     * @param newVersion the newest found version
     * @return the embed, as a {@link EmbedBuilder builder}
     */
    @Nonnull
    protected abstract EmbedBuilder getEmbed(@Nullable T oldVersion, T newVersion);

    /**
     * Runs this notifier.
     */
    @Override
    public final void run() {
        if (TheCommander.getInstance() == null) {
            LOGGER.warn(loggingMarker, "Cannot start {} update notifier due to the bot instance being null.", configuration.name);
            return;
        }
        if (!pickedUpFromDB) {
            final String oldData = TheCommander.getInstance().getJdbi()
                .withExtension(UpdateNotifiersDAO.class, db -> db.getLatest(configuration.name));
            final Runnable initialQuery = () -> {
                try {
                    final T queried = queryLatest();
                    if (queried != null) {
                        update(queried);
                    }
                } catch (IOException e) {
                    LOGGER.error(loggingMarker, "An exception occurred trying to resolve latest version: ", e);
                }
            };
            if (oldData != null) {
                try {
                    latest = configuration.serializer.deserialize(oldData);
                } catch (Exception ignored) {
                    // In the case of an exception encountered during serializing, consider updating as the database data never existed
                    initialQuery.run();
                }
            } else {
                initialQuery.run();
            }
            pickedUpFromDB = true;
        }

        LOGGER.debug(loggingMarker, "Checking for new versions...");
        final T old = latest;
        T newVersion = null;
        try {
            newVersion = queryLatest();
        } catch (IOException e) {
            LOGGER.error("Encountered exception trying to resolve latest version: ", e);
        }

        if (newVersion != null && (old == null || configuration.versionComparator.compare(old, newVersion) < 0)) {
            LOGGER.info(loggingMarker, "New release found, from {} to {}", old, newVersion);
            update(newVersion);

            final var embed = getEmbed(old, latest);

            //noinspection ConstantConditions
            configuration.channelGetter.apply(TheCommander.getInstance().getGeneralConfig().channels().updateNotifiers())
                .stream()
                .map(it -> it.resolve(id -> TheCommander.getJDA().getChannelById(BaseGuildMessageChannel.class, id)))
                .filter(Objects::nonNull)
                .forEach(channel -> {
                    embed.setTimestamp(Instant.now());
                    if (configuration.webhookInfo == null) {
                        channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                            if (channel.getType() == ChannelType.NEWS) {
                                msg.crosspost().queue();
                            }
                        });
                    } else {
                        getWebhookClient(channel)
                            .send(new WebhookMessageBuilder()
                                .setAvatarUrl(configuration.webhookInfo.avatarUrl())
                                .setUsername(configuration.webhookInfo.username())
                                .addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build())
                                .build())
                            .thenAccept(msg -> {
                                if (channel.getType() == ChannelType.NEWS) {
                                    channel.retrieveMessageById(msg.getId()).flatMap(Message::crosspost).queue();
                                }
                            });
                    }
                });
        } else {
            LOGGER.debug(loggingMarker, "No new version found");
        }
    }

    private void update(T latest) {
        this.latest = latest;
        TheCommander.getInstance().getJdbi().useExtension(UpdateNotifiersDAO.class, db -> db.setLatest(
            configuration.name, configuration.serializer.serialize(latest)
        ));
    }

    public static final ScheduledExecutorService WEBHOOKS_EXECUTOR = Executors.newScheduledThreadPool(1, r ->
        Utils.setThreadDaemon(new Thread(r, "UpdateNotifierWebhooks"), true));
    public static final Long2ObjectMap<JDAWebhookClient> WEBHOOKS = new Long2ObjectOpenHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
            WEBHOOKS.values().forEach(JDAWebhookClient::close), "UpdateNotifierWebhookCloser"));
    }

    public static final String WEBHOOK_NAME = "UpdateNotifiers";

    public static Webhook getOrCreateWebhook(BaseGuildMessageChannel channel) {
        final var alreadyExisted = channel.retrieveWebhooks()
            .complete()
            .stream()
            .filter(w -> w.getName().trim().equals(WEBHOOK_NAME))
            .findAny();
        return alreadyExisted.orElseGet(() -> channel.createWebhook(WEBHOOK_NAME).complete());
    }

    public static JDAWebhookClient getWebhookClient(BaseGuildMessageChannel channel) {
        return WEBHOOKS.computeIfAbsent(channel.getIdLong(), k ->
            WebhookClientBuilder.fromJDA(getOrCreateWebhook(channel))
                .setExecutorService(WEBHOOKS_EXECUTOR)
                .setHttpClient(TheCommander.OK_HTTP_CLIENT)
                .setAllowedMentions(AllowedMentions.none())
                .buildJDA());
    }

    @Data
    @Builder
    @SuppressWarnings("ClassCanBeRecord")
    public static final class NotifierConfiguration<T> {
        private final String name;
        private final Function<Configuration.Channels.UpdateNotifiers, List<SnowflakeValue>> channelGetter;
        private final Comparator<T> versionComparator;
        private final StringSerializer<T> serializer;
        private final WebhookInfo webhookInfo;

        public static <T> Comparator<T> notEqual() {
            return (v1, v2) -> v1.equals(v2) ? 0 : -1;
        }
    }

    public record WebhookInfo(String username, String avatarUrl) {
    }
}
