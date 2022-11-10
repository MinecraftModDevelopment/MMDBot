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
package com.mcmoddev.mmdbot.commander;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.cfwebhooks.CFProjects;
import com.mcmoddev.mmdbot.commander.cfwebhooks.CurseForgeManager;
import com.mcmoddev.mmdbot.commander.commands.EvaluateCommand;
import com.mcmoddev.mmdbot.commander.commands.GistCommand;
import com.mcmoddev.mmdbot.commander.commands.RoleSelectCommand;
import com.mcmoddev.mmdbot.commander.commands.curseforge.CurseForgeCommand;
import com.mcmoddev.mmdbot.commander.commands.menu.message.AddQuoteContextMenu;
import com.mcmoddev.mmdbot.commander.commands.menu.message.GistContextMenu;
import com.mcmoddev.mmdbot.commander.commands.menu.user.UserInfoContextMenu;
import com.mcmoddev.mmdbot.commander.commands.tricks.AddTrickCommand;
import com.mcmoddev.mmdbot.commander.commands.tricks.EditTrickCommand;
import com.mcmoddev.mmdbot.commander.commands.tricks.RunTrickCommand;
import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.config.GuildConfiguration;
import com.mcmoddev.mmdbot.commander.config.PermissionList;
import com.mcmoddev.mmdbot.commander.custompings.CustomPings;
import com.mcmoddev.mmdbot.commander.custompings.CustomPingsListener;
import com.mcmoddev.mmdbot.commander.docs.ConfigBasedElementLoader;
import com.mcmoddev.mmdbot.commander.docs.DocsCommand;
import com.mcmoddev.mmdbot.commander.docs.NormalDocsSender;
import com.mcmoddev.mmdbot.commander.eventlistener.FilePreviewListener;
import com.mcmoddev.mmdbot.commander.eventlistener.ReferencingListener;
import com.mcmoddev.mmdbot.commander.eventlistener.ThreadListener;
import com.mcmoddev.mmdbot.commander.migrate.QuotesMigrator;
import com.mcmoddev.mmdbot.commander.migrate.TricksMigrator;
import com.mcmoddev.mmdbot.commander.reminders.Reminders;
import com.mcmoddev.mmdbot.commander.reminders.SnoozingListener;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers;
import com.mcmoddev.mmdbot.commander.util.EventListeners;
import com.mcmoddev.mmdbot.commander.util.mc.MCVersions;
import com.mcmoddev.mmdbot.commander.util.oldchannels.OldChannelsHelper;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.commands.CommandUpserter;
import com.mcmoddev.mmdbot.core.commands.component.ComponentListener;
import com.mcmoddev.mmdbot.core.commands.component.ComponentManager;
import com.mcmoddev.mmdbot.core.commands.component.DeferredComponentListenerRegistry;
import com.mcmoddev.mmdbot.core.commands.component.storage.ComponentStorage;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.core.util.MessageUtilities;
import com.mcmoddev.mmdbot.core.util.ReflectionsUtils;
import com.mcmoddev.mmdbot.core.util.TaskScheduler;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.config.ConfigurateUtils;
import com.mcmoddev.mmdbot.core.util.config.SnowflakeValue;
import com.mcmoddev.mmdbot.core.util.dictionary.DictionaryUtils;
import com.mcmoddev.mmdbot.core.util.event.DismissListener;
import com.mcmoddev.mmdbot.core.util.event.OneTimeEventListener;
import de.ialistannen.javadocapi.querying.FuzzyElementQuery;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.matyrobbrt.curseforgeapi.CurseForgeAPI;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageRequest;
import okhttp3.OkHttpClient;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.sqlite.SQLiteDataSource;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.matyrobbrt.curseforgeapi.util.Utils.rethrowFunction;

public final class TheCommander implements Bot {
    static final TypeSerializerCollection ADDED_SERIALIZERS = TypeSerializerCollection.defaults()
        .childBuilder()
        .register(PermissionList.class, new PermissionList.Serializer())
        .register(SnowflakeValue.class, new SnowflakeValue.Serializer())
        .build();

    public static final Logger LOGGER = LoggerFactory.getLogger("TheCommander");
    public static final ScheduledExecutorService CURSE_FORGE_UPDATE_SCHEDULER = Executors.newScheduledThreadPool(1,
        r -> Utils.setThreadDaemon(new Thread(r, "CurseForgeUpdateChecker"), true));

    @RegisterBotType(name = BotRegistry.THE_COMMANDER_NAME)
    public static final BotType<TheCommander> BOT_TYPE = new BotType<>() {
        @Override
        public TheCommander createBot(final Path runPath) {
            try {
                return new TheCommander(runPath, DotenvLoader.builder()
                    .filePath(runPath.toAbsolutePath().resolve(".env"))
                    .whenCreated(writer -> writer
                        .writeComment("The token of the bot: ")
                        .writeValue("BOT_TOKEN", "")

                        .writeComment("The API key to use for CurseForge requests: ")
                        .writeValue("CF_API_KEY", "")

                        .writeComment("The OwlBot API Token used for dictionary lookup:")
                        .writeValue("OWL_BOT_TOKEN", "")

                        .writeComment("The token used for GitHub requests. (Mainly for creating gists)")
                        .writeValue("GITHUB_TOKEN", "")
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

    private static final Set<GatewayIntent> INTENTS = Set.of(
        GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_BANS,
        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS,
        GatewayIntent.MESSAGE_CONTENT
    );

    private static final Set<Message.MentionType> DEFAULT_MENTIONS = EnumSet.of(
        Message.MentionType.EMOJI,
        Message.MentionType.CHANNEL
    );

    private static final Set<Permission> PERMISSIONS = EnumSet.of(
        Permission.MESSAGE_MANAGE,
        Permission.MANAGE_WEBHOOKS,
        Permission.MANAGE_THREADS,
        Permission.MANAGE_ROLES
    );

    private static final Emoji COMMAND_RECEIVED = Emoji.fromUnicode("☑️");

    /**
     * The instance.
     */
    private static TheCommander instance;

    /**
     * The startup time of the bot.
     */
    private static Instant startupTime;

    public static Instant getStartupTime() {
        return startupTime;
    }

    /**
     * The bot's current version.
     *
     * <p>
     * The version will be taken from the {@code Implementation-Version} attribute of the JAR manifest. If that is
     * unavailable, the version shall be the combination of the string {@code "DEV "} and the current date and time
     * in {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE_TIME}.
     */
    public static final String VERSION;

    static {
        MessageRequest.setDefaultMentionRepliedUser(false);

        var version = TheCommander.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "DEV " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC));
        }
        VERSION = version;
    }

    public static TheCommander getInstance() {
        return instance;
    }

    public static JDA getJDA() {
        return getInstance() == null ? null : getInstance().getJda();
    }

    private static final DeferredComponentListenerRegistry LISTENER_REGISTRY = new DeferredComponentListenerRegistry();

    public static ComponentListener.Builder getComponentListener(final String featureId) {
        return LISTENER_REGISTRY.createListener(featureId);
    }

    private static final OneTimeEventListener<TaskScheduler.CollectTasksEvent> COLLECT_TASKS_LISTENER
        = new OneTimeEventListener<>(event -> event.addTask(() -> {
        if (getInstance() != null) {
            getInstance().getComponentManager().removeComponentsOlderThan(30, ChronoUnit.MINUTES);
        }
    }, 0, 15, TimeUnit.MINUTES));

    private JDA jda;
    private Jdbi jdbi;
    private CommandClient commandClient;
    private ComponentManager componentManager;
    @Nullable
    private CurseForgeManager curseForgeManager;
    private ConfigurationReference<CommentedConfigurationNode> config;
    private Configuration generalConfig;
    private final Dotenv dotenv;
    private final Path runPath;
    private final Long2ObjectMap<GuildConfiguration> guildConfigs = new Long2ObjectOpenHashMap<>();

    private final String githubToken;

    public TheCommander(final Path runPath, final Dotenv dotenv) {
        this.dotenv = dotenv;
        this.runPath = runPath;

        DictionaryUtils.setToken(dotenv.get("OWL_BOT_TOKEN", null));
        githubToken = dotenv.get("GITHUB_TOKEN", "");
    }

    @Override
    public void start() {
        instance = this;
        EventListeners.clear();
        UpdateNotifiers.register();

        try {
            final var configPath = runPath.resolve("configs").resolve("general_config.conf");
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .emitComments(true)
                .prettyPrinting(true)
                .defaultOptions(ConfigurationOptions.defaults().serializers(ADDED_SERIALIZERS))
                .path(configPath)
                .build();
            final var cPair =
                ConfigurateUtils.loadConfig(loader, configPath, c -> generalConfig = c, Configuration.class, Configuration.EMPTY);
            config = cPair.config();
            generalConfig = cPair.value().get();

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
            dataSource.setDatabaseName(BotRegistry.THE_COMMANDER_NAME);

            final var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:commander/db")
                .load();
            flyway.migrate();

            jdbi = Jdbi.create(dataSource);
        }

        // Setup components
        {
            final var storage = ComponentStorage.sql(jdbi, "components");
            componentManager = LISTENER_REGISTRY.createManager(storage);
            EventListeners.COMMANDS_LISTENER.addListener(componentManager);
        }

        if (generalConfig.bot().getOwners().isEmpty()) {
            LOGGER.warn("Please provide at least one bot owner!");
            throw new RuntimeException("Please provide at least one bot owner!");
        }
        final var coOwners = generalConfig.bot().getOwners()
            .subList(1, generalConfig.bot().getOwners().size())
            .stream()
            .map(SnowflakeValue::asString)
            .toList();

        commandClient = new CommandClientBuilder()
            .setOwnerId(generalConfig.bot().getOwners().get(0).asString())
            .setCoOwnerIds(coOwners.toArray(String[]::new))
            .setPrefixes(generalConfig.bot().getPrefixes().toArray(String[]::new))
            .addCommands(new GistCommand(), EvaluateCommand.COMMAND)
            .addContextMenus(new GistContextMenu(), new UserInfoContextMenu())
            .setManualUpsert(true)
            .useHelpBuilder(false)
            .setActivity(null)
            .setGuildSettingsManager(new GuildConfiguration.SettingManager(this::getConfigForGuild))
            .setListener(new CommandListener() {
                @Override
                public void onCommand(final CommandEvent event, final Command command) {
                    event.getMessage().addReaction(COMMAND_RECEIVED).queue();
                }
            })
            .build();
        EventListeners.COMMANDS_LISTENER.addListener((EventListener) commandClient);

        {
            record SlashCommandRegistration(Object fieldValue, RegisterSlashCommand annotation) {
            }

            // Command register
            ReflectionsUtils.getFieldsAnnotatedWith(RegisterSlashCommand.class)
                .stream()
                .peek(f -> f.setAccessible(true))
                .map(rethrowFunction(f -> new SlashCommandRegistration(f.get(null), f.getAnnotation(RegisterSlashCommand.class))))
                .map(pair -> {
                    final var object = pair.fieldValue();
                    if (object instanceof SlashCommand slash) {
                        if (pair.annotation().asPrefixCommand()) {
                            commandClient.addCommand(slash);
                        }
                        return slash;
                    } else if (object instanceof Supplier<?> sup) {
                        final var obj = sup.get();
                        if (obj instanceof SlashCommand slash) {
                            if (pair.annotation().asPrefixCommand()) {
                                commandClient.addCommand(slash);
                            }
                            return slash;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(commandClient::addSlashCommand);
        }

        final var upserter = new CommandUpserter(commandClient, generalConfig.bot().areCommandsForcedGuildOnly(),
            generalConfig.bot().guild(), PERMISSIONS);
        EventListeners.COMMANDS_LISTENER.addListeners(upserter);

        // Evaluation
        if (generalConfig.features().isEvaluationEnabled()) {
            EventListeners.COMMANDS_LISTENER.addListener(new EvaluateCommand.ModalListener());
        }

        // Tricks
        if (generalConfig.features().tricks().tricksEnabled()) {
            commandClient.addCommand(new AddTrickCommand.Prefix());
            commandClient.addCommand(new EditTrickCommand.Prefix());
            EventListeners.COMMANDS_LISTENER.addListeners(new AddTrickCommand.ModalListener());
            if (generalConfig.features().tricks().prefixEnabled()) {
                Tricks.getTricks().stream().map(RunTrickCommand.Prefix::new).forEach(commandClient::addCommand);
            }
        }

        // Quotes
        if (generalConfig.features().areQuotesEnabled()) {
            commandClient.addContextMenu(new AddQuoteContextMenu());
        }

        // Reminders
        if (generalConfig.features().reminders().areEnabled()) {
            Reminders.scheduleAllReminders();
            EventListeners.COMMANDS_LISTENER.addListeners(SnoozingListener.INSTANCE);
        }

        // Docs
        {
            try {
                final var loader = new ConfigBasedElementLoader(runPath.resolve("docs"));
                final var command = new DocsCommand(new FuzzyElementQuery(), loader, new NormalDocsSender(), getComponentListener("docs-cmd"));
                commandClient.addSlashCommand(command);
            } catch (Exception e) {
                LOGGER.error("Exception trying to load docs command! ", e);
            }
        }

        // Button listeners
        EventListeners.COMMANDS_LISTENER.addListeners(new DismissListener(), RoleSelectCommand.COMMAND);

        if (generalConfig.features().isReferencingEnabled()) {
            EventListeners.MISC_LISTENER.addListener(new ReferencingListener());
        }

        EventListeners.MISC_LISTENER.addListeners(new ThreadListener(), new FilePreviewListener());

        COLLECT_TASKS_LISTENER.register(Events.MISC_BUS);
        CurseForgeCommand.RG_TASK_SCHEDULER_LISTENER.register(Events.MISC_BUS);
        MCVersions.REFRESHER_TASK.register(Events.MISC_BUS);

        try {
            final var builder = JDABuilder
                .create(getToken(), INTENTS)
                .addEventListeners(listenerConsumer((ReadyEvent event) -> {
                    startupTime = Instant.now();
                    Events.MISC_BUS.addListener((final TaskScheduler.CollectTasksEvent ct) -> OldChannelsHelper.registerListeners(ct, event.getJDA()));
                }), CustomPingsListener.LISTENER.get())
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .setEnabledIntents(INTENTS);
            EventListeners.register(builder::addEventListeners);
            jda = builder.build().awaitReady();
        } catch (final InvalidTokenException exception) {
            LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }

        try {
            final var cfKey = dotenv.get("CF_API_KEY", "");
            if (!cfKey.isBlank()) {
                final var api = CurseForgeAPI
                    .builder()
                    .apiKey(cfKey)
                    .build();
                final var cfProjects = new CFProjects(runPath.resolve("cf_projects.json"));
                this.curseForgeManager = new CurseForgeManager(api, cfProjects);

                CURSE_FORGE_UPDATE_SCHEDULER.scheduleAtFixedRate(cfProjects, 0, 10, TimeUnit.MINUTES);
                CurseForgeCommand.REFRESH_GAMES_TASK.run();
            } else {
                LOGGER.warn("Could not find a valid CurseForge API Key! Some features might not work as expected.");
            }
        } catch (LoginException e) {
            LOGGER.error("Error while authenticating to the CurseForge API. Please provide a valid token, or don't provide any value!", e);
        }
    }

    @Override
    public void shutdown() {
        commandClient.shutdown();
        jda.shutdownNow();
        generalConfig = null;
        config = null;
        curseForgeManager = null;
        instance = null; // Clear the instance, as it doesn't exist anymore.
        // The "this" object should still exist for restarting it, at which point the instance will
        // be assigned again
    }

    @Override
    public void migrateData() throws IOException {
        new TricksMigrator(runPath).migrate();
        new QuotesMigrator(runPath).migrate();
        Reminders.MIGRATOR.migrate(Reminders.CURRENT_SCHEMA_VERSION, Reminders.PATH_RESOLVER.apply(runPath));
        CustomPings.MIGRATOR.migrate(CustomPings.CURRENT_SCHEMA_VERSION, CustomPings.PATH_RESOLVER.apply(runPath));
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    @Override
    public String getToken() {
        return dotenv.get("BOT_TOKEN");
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

    public CommandClient getCommandClient() {
        return commandClient;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public Optional<CurseForgeManager> getCurseForgeManager() {
        return Optional.ofNullable(curseForgeManager);
    }

    public Configuration getGeneralConfig() {
        return generalConfig;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    @Nullable
    public RestAction<Message> getMessageByLink(final String link) {
        return MessageUtilities.decodeMessageLink(link)
            .map(info -> {
                final var guild = getJda().getGuildById(info.guildId());
                if (guild == null) return null;
                final var channel = guild.getChannelById(MessageChannel.class, info.channelId());
                return channel == null ? null : channel.retrieveMessageById(info.messageId());
            })
            .orElse(null);
    }

    public String getGithubToken() {
        return githubToken;
    }

    public GuildConfiguration getConfigForGuild(long guildId) {
        return guildConfigs.computeIfAbsent(guildId, rethrowFunction(id -> {
            final var path = runPath.resolve("configs").resolve("guild").resolve(guildId + ".conf");
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder()
                .emitComments(true)
                .prettyPrinting(true)
                .defaultOptions(ConfigurationOptions.defaults().serializers(ADDED_SERIALIZERS))
                .path(path)
                .build();
            return ConfigurateUtils.loadConfig(loader, path, cfg -> guildConfigs.put(guildId, cfg), GuildConfiguration.class, GuildConfiguration.EMPTY)
                .value()
                .get();
        }));
    }

    public GuildConfiguration getConfigForGuild(Guild guild) {
        return getConfigForGuild(guild.getIdLong());
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
}
