module MMDBot {
    exports com.mcmoddev.mmdbot;

    requires annotations;
    requires com.electronwill.nightconfig.core;
    requires com.electronwill.nightconfig.toml;
    requires com.google.common;
    requires com.google.gson;
    requires java.desktop;
    requires java.sql;
    requires java.xml;
    requires jsr305;
    requires linkie.core;
    requires logback.classic;
    requires logback.core;
    requires net.dv8tion.jda;
    requires org.flywaydb.core;
    requires org.jdbi.v3.core;
    requires org.jdbi.v3.sqlobject;
    requires org.slf4j;
    requires org.xerial.sqlitejdbc;
    requires pw.chew.jdautilities.command;
    requires pw.chew.jdautilities.commons;
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core.jvm;
}
