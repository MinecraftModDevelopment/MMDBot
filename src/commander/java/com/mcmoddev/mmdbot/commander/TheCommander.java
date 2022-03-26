/*
 * MMDBot - https://github.com/MinecraftModDevelopment/MMDBot
 * Copyright (C) 2016-2022 <MMD - MinecraftModDevelopment>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
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

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.mcmoddev.mmdbot.commander.annotation.RegisterSlashCommand;
import com.mcmoddev.mmdbot.commander.cfwebhooks.CFProjects;
import com.mcmoddev.mmdbot.commander.cfwebhooks.CurseForgeManager;
import com.mcmoddev.mmdbot.commander.commands.DictionaryCommand;
import com.mcmoddev.mmdbot.commander.commands.EvaluateCommand;
import com.mcmoddev.mmdbot.commander.commands.GistCommand;
import com.mcmoddev.mmdbot.commander.commands.HelpCommand;
import com.mcmoddev.mmdbot.commander.commands.QuoteCommand;
import com.mcmoddev.mmdbot.commander.commands.RemindCommand;
import com.mcmoddev.mmdbot.commander.commands.RolesCommand;
import com.mcmoddev.mmdbot.commander.commands.curseforge.CurseForgeCommand;
import com.mcmoddev.mmdbot.commander.commands.menu.message.AddQuoteContextMenu;
import com.mcmoddev.mmdbot.commander.commands.menu.message.GistContextMenu;
import com.mcmoddev.mmdbot.commander.commands.menu.user.UserInfoContextMenu;
import com.mcmoddev.mmdbot.commander.commands.tricks.AddTrickCommand;
import com.mcmoddev.mmdbot.commander.commands.tricks.EditTrickCommand;
import com.mcmoddev.mmdbot.commander.commands.tricks.ListTricksCommand;
import com.mcmoddev.mmdbot.commander.commands.tricks.RunTrickCommand;
import com.mcmoddev.mmdbot.commander.config.Configuration;
import com.mcmoddev.mmdbot.commander.eventlistener.DismissListener;
import com.mcmoddev.mmdbot.commander.eventlistener.ReferencingListener;
import com.mcmoddev.mmdbot.commander.eventlistener.ThreadListener;
import com.mcmoddev.mmdbot.commander.migrate.QuotesMigrator;
import com.mcmoddev.mmdbot.commander.migrate.TricksMigrator;
import com.mcmoddev.mmdbot.commander.reminders.Reminders;
import com.mcmoddev.mmdbot.commander.reminders.SnoozingListener;
import com.mcmoddev.mmdbot.commander.tricks.Tricks;
import com.mcmoddev.mmdbot.commander.updatenotifiers.UpdateNotifiers;
import com.mcmoddev.mmdbot.commander.util.EventListeners;
import com.mcmoddev.mmdbot.commander.util.ThreadChannelCreatorEvents;
import com.mcmoddev.mmdbot.commander.util.mc.MCVersions;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.event.Events;
import com.mcmoddev.mmdbot.core.util.ConfigurateUtils;
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.core.util.MessageUtilities;
import com.mcmoddev.mmdbot.core.util.ReflectionsUtils;
import com.mcmoddev.mmdbot.core.util.Utils;
import com.mcmoddev.mmdbot.core.util.dictionary.DictionaryUtils;
import com.mcmoddev.mmdbot.core.util.jda.CommandUpserter;
import com.mcmoddev.mmdbot.dashboard.util.BotUserData;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.matyrobbrt.curseforgeapi.CurseForgeAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import javax.annotation.Nullable;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class TheCommander implements Bot {
    static final TypeSerializerCollection ADDED_SERIALIZERS = TypeSerializerCollection.defaults()
        .childBuilder()
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
                    .filePath(runPath.resolve(".env"))
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
        GatewayIntent.GUILD_EMOJIS,
        GatewayIntent.GUILD_MESSAGE_REACTIONS,
        GatewayIntent.GUILD_MESSAGES,
        GatewayIntent.GUILD_MEMBERS);

    private static final Set<Message.MentionType> DEFAULT_MENTIONS = EnumSet.of(
        Message.MentionType.EMOTE,
        Message.MentionType.CHANNEL);

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
        AllowedMentions.setDefaultMentionRepliedUser(false);

        var version = TheCommander.class.getPackage().getImplementationVersion();
        if (version == null) {
            version = "DEV " + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC));
        }
        VERSION = version;
    }

    public static void main(String[] args) {
        final var bot = BOT_TYPE.createBot(Path.of("thecommander"));
        bot.start();
    }

    public static TheCommander getInstance() {
        return instance;
    }

    public static JDA getJDA() {
        return getInstance() == null ? null : getInstance().getJda();
    }

    private JDA jda;
    private CommandClient commandClient;
    @Nullable
    private CurseForgeManager curseForgeManager;
    private ConfigurationReference<CommentedConfigurationNode> config;
    private Configuration generalConfig;
    private final Dotenv dotenv;
    private final Path runPath;

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
            config = cPair.second();
            generalConfig = cPair.first().get();

        } catch (ConfigurateException e) {
            LOGGER.error("Exception while trying to load general config", e);
            throw new RuntimeException(e);
        }

        MessageAction.setDefaultMentionRepliedUser(false);
        MessageAction.setDefaultMentions(DEFAULT_MENTIONS);

        if (generalConfig.bot().getOwners().isEmpty()) {
            LOGGER.warn("Please provide at least one bot owner!");
            throw new RuntimeException();
        }
        final var coOwners = generalConfig.bot().getOwners().subList(1, generalConfig.bot().getOwners().size());

        commandClient = new CommandClientBuilder()
            .setOwnerId(generalConfig.bot().getOwners().get(0))
            .setCoOwnerIds(coOwners.toArray(String[]::new))
            .setPrefixes(generalConfig.bot().getPrefixes().toArray(String[]::new))
            .addCommands(new GistCommand(), EvaluateCommand.COMMAND)
            .addContextMenus(new GistContextMenu(), new UserInfoContextMenu())
            .setManualUpsert(true)
            .useHelpBuilder(false)
            .setActivity(null)
            .build();
        EventListeners.COMMANDS_LISTENER.addListener((EventListener) commandClient);

        {
            // Command register
            ReflectionsUtils.getFieldsAnnotatedWith(RegisterSlashCommand.class)
                .stream()
                .peek(f -> f.setAccessible(true))
                .map(io.github.matyrobbrt.curseforgeapi.util.Utils.rethrowFunction(f -> f.get(null)))
                .map(object -> {
                    if (object instanceof SlashCommand slash) {
                        return slash;
                    } else if (object instanceof Supplier<?> sup) {
                        final var obj = sup.get();
                        if (obj instanceof SlashCommand slash) {
                            return slash;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(commandClient::addSlashCommand);
        }

        final var upserter = new CommandUpserter(commandClient, generalConfig.bot().areCommandsForcedGuildOnly(),
            generalConfig.bot().guild());
        EventListeners.COMMANDS_LISTENER.addListener(upserter);

        // Tricks
        if (generalConfig.features().tricks().tricksEnabled()) {
            commandClient.addCommand(new AddTrickCommand.Prefix());
            commandClient.addCommand(new EditTrickCommand.Prefix());
            EventListeners.COMMANDS_LISTENER.addListener(ListTricksCommand.getListListener());
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
            EventListeners.COMMANDS_LISTENER.addListeners(SnoozingListener.INSTANCE, RemindCommand.ListCmd.getListener());
        }

        // Button listeners
        EventListeners.COMMANDS_LISTENER.addListeners(DictionaryCommand.listener, new DismissListener(),
            QuoteCommand.ListQuotes.getQuoteListener(), RolesCommand.getListener(), HelpCommand.getListener());

        if (generalConfig.features().isReferencingEnabled()) {
            EventListeners.MISC_LISTENER.addListener(new ReferencingListener());
        }

        EventListeners.MISC_LISTENER.addListeners(new ThreadListener(),
            new ThreadChannelCreatorEvents(this::getGeneralConfig));
        CurseForgeCommand.RG_TASK_SCHEDULER_LISTENER.register(Events.MISC_BUS);
        MCVersions.REFRESHER_TASK.register(Events.MISC_BUS);

        try {
            final var builder = JDABuilder
                .create(dotenv.get("BOT_TOKEN"), INTENTS)
                .addEventListeners(new ListenerAdapter() {
                    @Override
                    public void onReady(@NotNull final ReadyEvent event) {
                        startupTime = Instant.now();
                        getLogger().warn("The Commander is ready to work! Logged in as {}", event.getJDA().getSelfUser().getAsTag());
                    }
                })
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .setEnabledIntents(INTENTS);
            EventListeners.register(builder::addEventListeners);
            jda = builder.build().awaitReady();
        } catch (final LoginException exception) {
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
        jda.shutdown();
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

    public CommandClient getCommandClient() {
        return commandClient;
    }

    public Optional<CurseForgeManager> getCurseForgeManager() {
        return Optional.ofNullable(curseForgeManager);
    }

    public Configuration getGeneralConfig() {
        return generalConfig;
    }

    @Nullable
    public RestAction<Message> getMessageByLink(final String link) throws MessageUtilities.MessageLinkException {
        final AtomicReference<RestAction<Message>> returnAtomic = new AtomicReference<>();
        MessageUtilities.decodeMessageLink(link, (guildId, channelId, messageId) -> {
            final var guild = getJDA().getGuildById(guildId);
            if (guild == null) return;
            final var channel = guild.getChannelById(MessageChannel.class, channelId);
            if (channel != null) {
                returnAtomic.set(channel.retrieveMessageById(messageId));
            }
        });
        return returnAtomic.get();
    }

    @Override
    public BotUserData getBotUserData() {
        final var selfUser = jda.getSelfUser();
        return new BotUserData(selfUser.getName(), selfUser.getDiscriminator(),
            selfUser.getAvatarUrl() == null ? selfUser.getDefaultAvatarUrl() : selfUser.getAvatarUrl());
    }

    public String getGithubToken() {
        return githubToken;
    }
}
