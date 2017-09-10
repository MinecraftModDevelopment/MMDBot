package com.mcmoddev.bot.handlers;

import java.awt.Color;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LoggingHandler {

    private static final int ERROR = Color.RED.getRGB();
    private static final int WARN = Color.YELLOW.getRGB();
    private static final int INFO = Color.WHITE.getRGB();
    private static final int DEBUG = Color.GRAY.getRGB();
    private static final int UNKNOWN = Color.PINK.getRGB();

    public LoggingHandler () {

        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }
}