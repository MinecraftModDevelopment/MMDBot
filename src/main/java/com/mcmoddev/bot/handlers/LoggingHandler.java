package com.mcmoddev.bot.handlers;

import java.awt.Color;

import org.slf4j.LoggerFactory;

import com.mcmoddev.bot.MMDBot;
import com.mcmoddev.bot.util.Utilities;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import sx.blah.discord.util.EmbedBuilder;

public class LoggingHandler {

    public LoggingHandler () {
        
        final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.TRACE);
        LoggerContext context = (LoggerContext) rootLogger.getLoggerContext();
        ConsoleAppender appender = new ConsoleAppender();
        appender.setContext(context);
        appender.start();
        rootLogger.addAppender(appender);
    }
    
    public static void setLoggerLevel(Logger logger, Level level) {
        
        logger.setLevel(level);
    }
    
    public static class ConsoleAppender extends AppenderBase<ILoggingEvent> {

        private static final int ERROR = Color.RED.getRGB();
        private static final int WARN = Color.YELLOW.getRGB();
        private static final int INFO = Color.WHITE.getRGB();
        private static final int DEBUG = Color.GRAY.getRGB();
        private static final int UNKNOWN = Color.PINK.getRGB();
        
        @Override
        protected void append (ILoggingEvent event) {
            
            if (MMDBot.state.isReady()) {
                
                final Level level = event.getLevel();
                final EmbedBuilder embed = new EmbedBuilder();
                
                if (level == Level.DEBUG && !event.getLoggerName().equalsIgnoreCase("MMDBot")) {
                    
                    return;
                }
                
                if (level == Level.TRACE && event.getLoggerName().equalsIgnoreCase("sx.blah.discord.Discord4J")) {
                    
                    return;
                }
                
                embed.withTitle(level.levelStr);
                
                embed.withColor(level == Level.DEBUG ? DEBUG : level == Level.ERROR || level == Level.TRACE ? ERROR : level == Level.INFO ? INFO : level == Level.WARN ? WARN : UNKNOWN);
                
                embed.withDesc(event.getFormattedMessage() + " - Logger: " + event.getLoggerName());
                
                if (event.getThrowableProxy() != null) {
                    
                    embed.appendDesc(Utilities.SEPERATOR + event.getThrowableProxy().getMessage());
                }
                
                Utilities.sendMessage(MMDBot.state.getDebugChannel(), embed.build());
            }
        }
    }
}