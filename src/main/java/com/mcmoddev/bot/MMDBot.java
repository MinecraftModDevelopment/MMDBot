package com.mcmoddev.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcmoddev.bot.command.CommandGuild;
import com.mcmoddev.bot.command.moderative.CommandReload;

import ch.qos.logback.classic.Level;
import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.ManagerCommands;
import net.darkhax.botbase.commands.mcp.CommandMCP;
import net.darkhax.botbase.commands.mcp.MCPData;
import sx.blah.discord.Discord4J;
import sx.blah.discord.handle.obj.IUser;

public class MMDBot extends BotBase {

    public static final Logger LOG = LoggerFactory.getLogger("MMDBot");
    public static MMDBot instance;

    public static void main (String... args) {

        final Configuration config = Configuration.getConfig();

        LOG.info("Turning off Discord4J logger.");
        ((ch.qos.logback.classic.Logger) Discord4J.LOGGER).setLevel(Level.OFF);;
        
        MCPData.load();
        instance = new MMDBot("MMDBot", config.getDiscordToken(), config.getCommandKey());
        instance.login();
    }

    public MMDBot (String botName, String auth, String commandKey) {

        super(botName, auth, commandKey, LOG);
        instance = this;
    }
    
    @Override
    public boolean isModerator (IUser user) {

        return false;
    }

    @Override
    public boolean isAdminUser (IUser user) {

        final long id = user.getLongID();
        return id == 137952759914823681L || id == 79179147875721216L;
    }

    @Override
    public void registerCommands (ManagerCommands handler) {

        handler.registerCommand("reload", new CommandReload());
        handler.registerCommand("guild", new CommandGuild());
        handler.registerCommand("mcp", new CommandMCP());
    }

    @Override
    public void reload () {

        super.reload();
        MCPData.load();
    }
}