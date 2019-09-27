package com.mcmoddev.bot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.mcmoddev.bot.commands.locked.info.CmdGuild;
import com.mcmoddev.bot.commands.locked.info.CmdRoles;
import com.mcmoddev.bot.commands.locked.info.CmdUser;
import com.mcmoddev.bot.commands.unlocked.CmdJustAsk;
import com.mcmoddev.bot.commands.unlocked.CmdPaste;
import com.mcmoddev.bot.commands.unlocked.CmdXy;
import com.mcmoddev.bot.commands.unlocked.search.CmdBing;
import com.mcmoddev.bot.commands.unlocked.search.CmdDuckDuckGo;
import com.mcmoddev.bot.commands.unlocked.search.CmdGoogle;
import com.mcmoddev.bot.commands.unlocked.search.CmdLmgtfy;
import com.mcmoddev.bot.events.MiscEvents;
import com.mcmoddev.bot.events.users.EventNicknameChanged;
import com.mcmoddev.bot.events.users.EventRoleAdded;
import com.mcmoddev.bot.events.users.EventRoleRemoved;
import com.mcmoddev.bot.events.users.EventUserJoined;
import com.mcmoddev.bot.events.users.EventUserLeft;
import com.mcmoddev.bot.misc.BotConfig;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class MMDBot {

    private static final String NAME = "MMDBot";
    private static final String VERSION = "3.0";
    private static final String ISSUE_TRACKER = "https://github.com/minecraftmoddevelopment/MMDBot/issues/";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static void main(String[] args) {
        try {
            JDABuilder botBuilder = new JDABuilder(AccountType.BOT).setToken(BotConfig.getConfig().getBotToken());

            botBuilder.addEventListeners(new EventUserJoined());
            botBuilder.addEventListeners(new EventUserLeft());
            botBuilder.addEventListeners(new EventNicknameChanged());
            botBuilder.addEventListeners(new EventRoleAdded());
            botBuilder.addEventListeners(new EventRoleRemoved());
            botBuilder.addEventListeners(new MiscEvents());
            botBuilder.setActivity(Activity.watching(BotConfig.getConfig().getBotTextStatus()));

            CommandClientBuilder commandBuilder = new CommandClientBuilder();

            commandBuilder.setOwnerId("141990014346199040");
            commandBuilder.setPrefix(BotConfig.getConfig().getPrefix());
            commandBuilder.addCommand(new CmdGuild());
            commandBuilder.addCommand(new CmdUser());
            commandBuilder.addCommand(new CmdRoles());
            commandBuilder.addCommand(new CmdJustAsk());
            commandBuilder.addCommand(new CmdPaste());
            commandBuilder.addCommand(new CmdXy());
            commandBuilder.addCommand(new CmdBing());
            commandBuilder.addCommand(new CmdDuckDuckGo());
            commandBuilder.addCommand(new CmdGoogle());
            commandBuilder.addCommand(new CmdLmgtfy());
            commandBuilder.setHelpWord("help");

            CommandClient commandListener = commandBuilder.build();
            botBuilder.addEventListeners(commandListener);
            botBuilder.build();

        } catch (LoginException exception) {
            LOGGER.error("Error logging in the bot! Please give the bot a token in the config file.", exception);
            System.exit(1);
        }
    }
}
