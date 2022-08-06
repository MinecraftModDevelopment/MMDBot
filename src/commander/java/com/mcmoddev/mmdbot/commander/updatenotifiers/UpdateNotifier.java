package com.mcmoddev.mmdbot.commander.updatenotifiers;

import com.mcmoddev.mmdbot.commander.TheCommander;
import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.util.StringSerializer;
import com.mcmoddev.mmdbot.commander.util.dao.UpdateNotifiersDAO;
import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import lombok.Builder;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
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

    protected UpdateNotifier(final NotifierConfiguration<T> configuration) {
        this.configuration = configuration;
        this.loggingMarker = MarkerFactory.getMarker(configuration.name);
        final String oldData = TheCommander.getInstance().getJdbi()
            .withExtension(UpdateNotifiersDAO.class, db -> db.getLatest(configuration.name));
        if (oldData != null) {
            latest = configuration.serializer.deserialize(oldData);
        } else {
            try {
                final T queried = queryLatest();
                if (queried != null) {
                    update(queried);
                }
            } catch (IOException e) {
                LOGGER.error(loggingMarker, "An exception occurred trying to resolve latest version: ", e);
            }
        }
    }

    /**
     * Queries the latest version of the listened project.
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

            //noinspection ConstantConditions
            configuration.channelGetter.apply(TheCommander.getInstance().getGeneralConfig().channels().updateNotifiers())
                .stream()
                .map(it -> it.resolve(id -> TheCommander.getJDA().getChannelById(MessageChannel.class, id)))
                .filter(Objects::nonNull)
                .forEach(channel -> {
                    final var embed = getEmbed(old, latest);
                    embed.setTimestamp(Instant.now());
                    channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                        if (channel.getType() == ChannelType.NEWS) {
                            msg.crosspost().queue();
                        }
                    });
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

    @Data
    @Builder
    @SuppressWarnings("ClassCanBeRecord")
    public static final class NotifierConfiguration<T> {
        private final String name;
        private final Function<Configuration.Channels.UpdateNotifiers, List<SnowflakeValue>> channelGetter;
        private final Comparator<T> versionComparator;
        private final StringSerializer<T> serializer;

        public static <T> Comparator<T> notEqual() {
            // noinspection ComparatorMethodParameterNotUsed
            return (v1, v2) -> v1.equals(v2) ? -1 : 0;
        }
    }
}
