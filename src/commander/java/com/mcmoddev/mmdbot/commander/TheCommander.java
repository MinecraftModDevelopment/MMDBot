package com.mcmoddev.mmdbot.commander;

import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.nio.file.Path;
import java.util.Set;

public final class TheCommander implements Bot {

    public static final Logger LOGGER = LoggerFactory.getLogger("TheCommander");

    @RegisterBotType(name = "thecommander")
    public static final BotType<TheCommander> BOT_TYPE = new BotType<>() {
        @Override
        public TheCommander createBot(final Path runPath) {
            return new TheCommander(runPath, Dotenv.configure()
                .directory(runPath.toString())
                .load());
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

    static {
        AllowedMentions.setDefaultMentionRepliedUser(false);
    }

    /**
     * The instance.
     */
    private static TheCommander instance;

    public static TheCommander getInstance() {
        return instance;
    }

    public static JDA getJDA() {
        return getInstance().getJda();
    }

    private JDA jda;
    private final Dotenv dotenv;
    private final Path runPath;

    public TheCommander(final Path runPath, final Dotenv dotenv) {
        this.dotenv = dotenv;
        this.runPath = runPath;
    }

    @Override
    public void start() {
        instance = this;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> LOGGER.warn("The bot is shutting down!")));

        try {
            jda = JDABuilder
                .create(dotenv.get("BOT_TOKEN"), INTENTS)
                .disableCache(CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY)
                .disableCache(CacheFlag.CLIENT_STATUS)
                .disableCache(CacheFlag.ONLINE_STATUS)
                .build().awaitReady();

        } catch (final LoginException exception) {
            LOGGER.error("Error logging in the bot! Please give the bot a valid token in the config file.", exception);
            System.exit(1);
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }
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
}
