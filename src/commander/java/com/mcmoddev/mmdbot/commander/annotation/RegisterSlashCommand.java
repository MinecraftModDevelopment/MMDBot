package com.mcmoddev.mmdbot.commander.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a field whose underlying object is an instance of {@link com.jagrosh.jdautilities.command.SlashCommand}
 * in order to register it when {@link com.mcmoddev.mmdbot.commander.TheCommander} is started up.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegisterSlashCommand {
}
