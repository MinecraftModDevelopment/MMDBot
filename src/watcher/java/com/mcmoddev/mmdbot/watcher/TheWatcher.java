/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * Specifically version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 * https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 */
package com.mcmoddev.mmdbot.watcher;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.commands.CommandUpserter;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.DeferredComponentListenerRegistry;
import com.mcmoddev.mmdbot.core.commands.component.storage.ComponentStorage;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.core.util.config.ConfigurateUtils;
import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import com.mcmoddev.mmdbot.core.util.event.ThreadedEventListener;
import com.mcmoddev.mmdbot.watcher.commands.information.InviteCommand;
import com.mcmoddev.mmdbot.watcher.commands.moderation.BanCommand;
import com.mcmoddev.mmdbot.watcher.commands.moderation.KickCommand;
import com.mcmoddev.mmdbot.watcher.commands.moderation.MuteCommand;
import com.mcmoddev.mmdbot.watcher.commands.moderation.ReactCommand;
import com.mcmoddev.mmdbot.watcher.commands.moderation.UnbanCommand;
import com.mcmoddev.mmdbot.watcher.commands.moderation.UnmuteCommand;
import com.mcmoddev.mmdbot.watcher.commands.moderation.WarningCommand;
import com.mcmoddev.mmdbot.watcher.event.EventReactionAdded;
import com.mcmoddev.mmdbot.watcher.event.ForumListener;
import com.mcmoddev.mmdbot.watcher.event.PersistedRolesEvents;
import com.mcmoddev.mmdbot.watcher.punishments.PunishableActions;
import com.mcmoddev.mmdbot.watcher.punishments.Punishment;
import com.mcmoddev.mmdbot.watcher.rules.RuleAgreementChecker;
import com.mcmoddev.mmdbot.watcher.rules.RuleCommand;
import com.mcmoddev.mmdbot.watcher.rules.UpdateRulesCommand;
import com.mcmoddev.mmdbot.watcher.serveravatar.ServerAvatarCommand;
import com.mcmoddev.mmdbot.watcher.util.BotConfig;
import com.mcmoddev.mmdbot.watcher.util.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.matyrobbrt.curseforgeapi.util.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.sqlite.SQLiteDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class TheWatcher implements Bot {
    static final TypeSerializerCollection ADDED_SERIALIZERS = TypeSerializerCollection.defaults()
        .childBuilder()
        .register(Punishment.class, new Punishment.Serializer())
        .register(SnowflakeValue.class, new SnowflakeValue.Serializer())
        .build();

    public static final Logger LOGGER = LoggerFactory.getLogger("TheWatcher");

    @RegisterBotType(name = BotRegistry.THE_WATCHER_NAME)
    public static final BotType<TheWatcher> BOT_TYPE = new BotType<>() {
        @Override
        public TheWatcher createBot(final Path runPath) {
            try {
                return new TheWatcher(runPath, DotenvLoader.builder()
                    .filePath(runPath.toAbsolutePath().resolve(".env"))
                    .whenCreated(writer -> writer
                        .writeComment("The token of the bot: ")
                        .writeValue("BOT_TOKEN", "")
                    )
                    .load());
            } catch (IOException e) {
                LOGGER.error("Could not load the .env file due to an IOException: ", e);
            }
            return null;
        }

        @Override
        public Logger getLogger() {
            return LOGGER;
        }
    };

    public static final ThreadGroup THREAD_GROUP = new ThreadGroup("The Watcher");

    public static final ThreadedEventListener COMMANDS_LISTENER = Utils.makeWithSupplier(() -> {
        final var group = new ThreadGroup(THREAD_GROUP, "Command Listeners");
        final var poll = (ThreadPoolExecutor) Executors.newFixedThreadPool(2, r ->
            com.mcmoddev.mmdbot.core.util.Utils.setThreadDaemon(new Thread(group, r, "CommandListener #%s".formatted(group.activeCount())),
                true));
        poll.setKeepAliveTime(30, TimeUnit.MINUTES);
        poll.allowCoreThreadTimeOut(true);
        return new ThreadedEventListener(poll);
    });
    public static final ThreadedEventListener MISC_LISTENER = Utils.makeWithSupplier(() -> {
        final var poll = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, r ->
            com.mcmoddev.mmdbot.core.util.Utils.setThreadDaemon(new Thread(THREAD_GROUP, r, "MiscEventsListener"),
                true));
        poll.setKeepAliveTime(30, TimeUnit.MINUTES);
        poll.allowCoreThreadTimeOut(true);
        return new ThreadedEventListener(poll);
    });
    public static final ThreadedEventListener PUNISHABLE_ACTIONS_LISTENER = new ThreadedEventListener(
        Executors.newFixedThreadPool(2, r -> com.mcmoddev.mmdbot.core.util.Utils.setThreadDaemon(new Thread(TheWatcher.THREAD_GROUP, r, "PunishableActions"), true))
    );

    private static final Set<GatewayIntent> INTENTS = Set.of(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_BANS,
        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.MESSAGE_CONTENT
    );

    static {
        MessageRequest.setDefaultMentionRepliedUser(false);
    }

    private static final DeferredComponentListenerRegistry LISTENER_REGISTRY = new DeferredComponentListenerRegistry();

    public static ComponentListener.Builder getComponentListener(final String featureId) {
        return LISTENER_REGISTRY.createListener(featureId);
    }

    /**
     * The instance.
     */
    private static TheWatcher instance;

    public static TheWatcher getInstance() {
        return instance;
    }

    public static JDA getJDA() {
        return getInstance().getJda();
    }

    private JDA jda;
    private Jdbi jdbi;
    private CommandClient commandClient;
    private Configuration config;
    private BotConfig oldConfig;
    private ConfigurationReference<CommentedConfigurationNode> configRef;
    private final Dotenv dotenv;
    private final Path runPath;

    public TheWatcher(final Path runPath, final Dotenv dotenv) {
        this.dotenv = dotenv;
        this.runPath = runPath;
    }

    @Override
    public void start() {
        instance = this;
        oldConfig = new BotConfig(runPath.resolve("old_config.toml"));
        Events.MISC_BUS.addListener((final TaskScheduler.CollectTasksEvent event) -> event.addTask(new RuleAgreementChecker(this::getJda),
            0, 1, TimeUnit.DAYS));

        try {
            final var configPath = runPath.resolve("config.conf");
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .emitComments(true)
                .prettyPrinting(true)
                .path(configPath)
                .defaultOptions(ops -> ops.serializers(ADDED_SERIALIZERS))
                .build();
            Objects.requireNonNull(loader.defaultOptions().serializers().get(Punishment.class));
            final var cPair =
                ConfigurateUtils.loadConfig(loader, configPath, c -> {
                    config = c;
                    PUNISHABLE_ACTIONS_LISTENER.clear();
                    PUNISHABLE_ACTIONS_LISTENER.addListeners(PunishableActions.getEnabledActions(c.punishments()));
                }, Configuration.class, Configuration.EMPTY);
            configRef = cPair.config();
            config = Objects.requireNonNull(cPair.value().get());
            PUNISHABLE_ACTIONS_LISTENER.addListeners(PunishableActions.getEnabledActions(config.punishments()));
        } catch (ConfigurateException e) {
            LOGGER.error("Exception while trying to load general config", e);
            throw new RuntimeException(e);
        }

        // Setup database
        {
            final var dbPath = getRunPath().resolve("data.db");
            if (!Files.exists(dbPath)) {
                try {
                    Files.createFile(dbPath);
                } catch (IOException e) {
                    throw new RuntimeException("Exception creating database!", e);
                }
            }
            final var url = "jdbc:sqlite:" + dbPath;
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(url);
            dataSource.setDatabaseName(BotRegistry.THE_WATCHER_NAME);

            final var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:watcher/db")
                .load();
            flyway.migrate();

            jdbi = Jdbi.create(dataSource);
        }

        // Setup components
        {
            final var storage = ComponentStorage.sql(jdbi, "components");
            final var componentManager = LISTENER_REGISTRY.createManager(storage);
            COMMANDS_LISTENER.addListener(componentManager);
        }

        if (config.bot().getOwners().isEmpty()) {
            LOGGER.warn("Please provide at least one bot owner!");
            throw new RuntimeException("Please provide at least one bot owner!");
        }
        final var coOwners = config.bot().getOwners().subList(1, config.bot().getOwners().size());

        commandClient = new CommandClientBuilder()
            .setOwnerId(config.bot().getOwners().get(0).asString())
            .setCoOwnerIds(coOwners
                .stream()
                .map(SnowflakeValue::asString)
                .toArray(String[]::new))
            .setPrefixes(config.bot().getPrefixes().toArray(String[]::new))
            .setManualUpsert(true)
            .useHelpBuilder(false)
            .setActivity(null)
            .addSlashCommands(new MuteCommand(), new UnmuteCommand(), new InviteCommand(), new WarningCommand(), new UpdateRulesCommand(), RuleCommand.INSTANCE, new ServerAvatarCommand())
            .addCommands(new BanCommand(), new UnbanCommand(), new ReactCommand(), new KickCommand(), RuleCommand.INSTANCE)
            .build();
        COMMANDS_LISTENER.addListener((EventListener) commandClient);

        final var upserter = new CommandUpserter(commandClient, config.bot().areCommandsForcedGuildOnly(),
            config.bot().guild());
        COMMANDS_LISTENER.addListener(upserter);

        // Buttons
        COMMANDS_LISTENER.addListener(new DismissListener());

        MISC_LISTENER.addListener(UpdateRulesCommand::onEvent);
        MISC_LISTENER.addListeners(new EventReactionAdded(), new PersistedRolesEvents(), new ForumListener());

        try {
            final var builder = JDABuilder
                .create(dotenv.get("BOT_TOKEN"), INTENTS)
                .addEventListeners(COMMANDS_LISTENER, MISC_LISTENER, PUNISHABLE_ACTIONS_LISTENER)
                .setActivity(Activity.of(oldConfig.getActivityType(), oldConfig.getActivityName()))
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .enableCache(CacheFlag.FORUM_TAGS)
                .setEnabledIntents(INTENTS);
            jda = builder.build().awaitReady();
        } catch (final InvalidTokenException exception) {
            LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }
    }

    @Override
    public void shutdown() {
        jda.shutdown();
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    public Dotenv getDotenv() {
        return dotenv;
    }

    public Path getRunPath() {
        return runPath;
    }

    public JDA getJda() {
        return jda;
    }

    public Configuration getConfig() {
        return config;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public CommandClient getCommandClient() {
        return commandClient;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Event> EventListener listenerConsumer(final Consumer<E> listener) {
        return event -> {
            final var type = (Class<E>) net.jodah.typetools.TypeResolver.resolveRawArgument(Consumer.class, listener.getClass());
            if (type.isInstance(event)) {
                listener.accept((E) event);
            }
        };
    }

    public static boolean isBotMaintainer(final Member member) {
        final var maintainers = getInstance().getConfig().roles().getBotMaintainers();
        return member.getRoles()
            .stream()
            .anyMatch(r -> {
                for (final var m : maintainers) {
                    if (m.test(r)) {
                        return true;
                    }
                }
                return false;
            });
    }

    public static Jdbi database() {
        return getInstance().getJdbi();
    }

    public static BotConfig getOldConfig() {
        return getInstance().oldConfig;
    }
}
