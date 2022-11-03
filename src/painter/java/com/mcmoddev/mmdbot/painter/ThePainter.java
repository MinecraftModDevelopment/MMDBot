package com.mcmoddev.mmdbot.painter;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.mmdbot.core.bot.Bot;
import com.mcmoddev.mmdbot.core.bot.BotRegistry;
import com.mcmoddev.mmdbot.core.bot.BotType;
import com.mcmoddev.mmdbot.core.bot.RegisterBotType;
import com.mcmoddev.mmdbot.core.util.DotenvLoader;
import com.mcmoddev.mmdbot.painter.servericon.ServerIconCommand;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class ThePainter implements Bot {
    public static final Logger LOGGER = LoggerFactory.getLogger("ThePainter");

    @RegisterBotType(name = BotRegistry.THE_PAINTER_NAME)
    public static final BotType<ThePainter> BOT_TYPE = new BotType<>() {
        @Override
        public ThePainter createBot(final Path runPath) {
            try {
                return new ThePainter(runPath, DotenvLoader.builder()
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

    private static ThePainter instance;
    public static ThePainter getInstance() {
        return instance;
    }

    private final Dotenv dotenv;
    private final Path runPath;

    private JDA jda;

    public ThePainter(final Path runPath, final Dotenv dotenv) {
        this.dotenv = dotenv;
        this.runPath = runPath;
    }

    @Override
    public void start() {
        instance = this;

        final var commandClient = new CommandClientBuilder()
            .setOwnerId("000000000000")
            .useHelpBuilder(false)
            .setActivity(null)
            .addSlashCommands(new ServerIconCommand())
            .build();

        try {
            final var builder = JDABuilder
                .createLight(dotenv.get("BOT_TOKEN"), Set.of())
                .addEventListeners(commandClient);
            jda = builder.build().awaitReady();
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting caching.", e);
            System.exit(1);
        }
    }

    @Override
    public BotType<?> getType() {
        return BOT_TYPE;
    }

    public JDA getJDA() {
        return this.jda;
    }

    public Path getRunPath() {
        return runPath;
    }

    @Override
    public void shutdown() {
        instance = null;
        jda.shutdownNow();
    }
}
