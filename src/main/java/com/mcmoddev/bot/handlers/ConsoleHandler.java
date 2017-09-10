package com.mcmoddev.bot.handlers;

import java.awt.Color;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import com.google.common.base.Throwables;
import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

@Plugin(name = "ConsoleHandler", category = "Core", elementType = "appender", printObject = true)
public class ConsoleHandler extends AbstractAppender {

    private static final int ERROR = Color.RED.getRGB();
    private static final int WARN = Color.YELLOW.getRGB();
    private static final int INFO = Color.WHITE.getRGB();
    private static final int DEBUG = Color.GRAY.getRGB();
    private static final int UNKNOWN = Color.PINK.getRGB();

    public ConsoleHandler () {

        super("ConsoleHandler", null, null);

        // TODO this probably isn't the best way to do this.
        final Logger logger = (Logger) LogManager.getRootLogger();
        this.start();
        logger.addAppender(this);
    }

    @Override
    public void append (LogEvent event) {

        if (MMDBot.isReady)
            Utilities.sendMessage(MMDBot.console, this.createMessage(event.getLevel(), event.getMessage().getFormattedMessage(), event.getThrown()));
    }

    private EmbedObject createMessage (Level level, String message, Throwable throwable) {

        final EmbedBuilder builder = new EmbedBuilder();

        builder.withTitle(level.name());
        builder.withColor(this.getColor(level));
        builder.withDescription(message);

        if (throwable != null)
            builder.appendField("Exception", Throwables.getStackTraceAsString(throwable), true);

        return builder.build();
    }

    private int getColor (Level level) {

        return level == Level.ERROR ? ERROR : level == Level.WARN ? WARN : level == Level.INFO ? INFO : level == Level.DEBUG ? DEBUG : UNKNOWN;
    }
}