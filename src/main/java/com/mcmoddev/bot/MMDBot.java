package com.mcmoddev.bot;

import java.io.File;

import com.mcmoddev.bot.command.*;
import com.mcmoddev.bot.curse.CurseTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcmoddev.bot.command.moderative.CommandBackup;
import com.mcmoddev.bot.command.moderative.CommandKill;
import com.mcmoddev.bot.command.moderative.CommandOldChannels;
import com.mcmoddev.bot.command.moderative.CommandReload;
import com.mcmoddev.bot.command.moderative.CommandUser;
import com.mcmoddev.bot.lib.ScheduledTimer;

import ch.qos.logback.classic.Level;
import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.ManagerCommands;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.*;

public class MMDBot extends BotBase {

    public static final Logger LOG = LoggerFactory.getLogger("MMDBot");
    public static final File DATA_DIR = new File("data/");
    public static MMDBot instance;

    private final Configuration config;
    private IRole admin;
    private IRole moderator;
    private IRole botAdmin;

    public final ScheduledTimer timer;
    public CurseTracker curseTracker;
    
    public static void main (String... args) {

        try {

            // Creates the data directory if it doesn't exist already.
            if (!DATA_DIR.exists()) {

                DATA_DIR.mkdirs();
            }

            // Restrict the discord4j logger to errors only
            if (Discord4J.LOGGER instanceof ch.qos.logback.classic.Logger) {

                LOG.info("Restricting Discord4J's logger to errors!");
                ((ch.qos.logback.classic.Logger) Discord4J.LOGGER).setLevel(Level.ERROR);
            }

            // Create bot, config, and login
            instance = new MMDBot("MMDBot", Configuration.getConfig());
            instance.login();
        }

        catch (final Exception e) {

            LOG.trace("Unable to launch bot!", e);
        }
    }

    public MMDBot (String botName, Configuration config) {

        super(botName, config.getDiscordToken(), config.getCommandKey(), LOG);
        instance = this;
        this.timer = new ScheduledTimer();
        this.config = config;
    }

    @Override
    public boolean isModerator (IGuild guild, IUser user) {

        return user.hasRole(this.moderator);
    }

    @Override
    public boolean isAdminUser (IGuild guild, IUser user) {

        return user.hasRole(this.admin) || user.hasRole(this.botAdmin);
    }

    @Override
    public void registerCommands (ManagerCommands handler) {

        // Moderative
        handler.registerCommand("reload", new CommandReload());
        handler.registerCommand("kill", new CommandKill());
        handler.registerCommand("oldchans", new CommandOldChannels());
        handler.registerCommand("user", new CommandUser());
        handler.registerCommand("backup", new CommandBackup());

        // Misc
        handler.registerCommand("guild", new CommandGuild());
        handler.registerCommand("xy", new CommandXY());
        handler.registerCommand("help", new CommandHelp());
        handler.registerCommand("me", new CommandMe());
        handler.registerCommand("curse", new CommandCurse());
        handler.registerCommand("greatmoves", new CommandGreatMoves());
        handler.registerCommand("mod", new CommandMod());
        handler.registerCommand("top", new CommandTop());
    
    }

    @Override
    public void onFailedLogin (IDiscordClient instance) {

        // No use
    }

    @Override
    public void onSucessfulLogin (IDiscordClient instance) {

        this.admin = instance.getRoleByID(176781877682634752L);
        this.moderator = instance.getRoleByID(178772974990655489L);
        this.botAdmin = instance.getRoleByID(226067502977777664L);
        this.curseTracker = new CurseTracker(this);
       
        
    }

    @Override
    public void reload () {

        super.reload();
        this.curseTracker.updateCurseData();
    }

    public Configuration getConfiguration () {

        return this.config;
    }
}