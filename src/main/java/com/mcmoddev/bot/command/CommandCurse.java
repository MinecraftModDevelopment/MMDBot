package com.mcmoddev.bot.command;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.mcmoddev.bot.cursemeta.MessageMods;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.Command;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandCurse implements Command {

    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {

        final Set<String> authors = new HashSet<>();

        for (final String param : params) {

            if (param.isEmpty()) {

                continue;
            }

            // The name "me" is treated as the discord name of the sender.
            if (param.equalsIgnoreCase("me")) {

                authors.add(message.getAuthor().getDisplayName(channel.getGuild()));
            }

            // The name "jarhax" is treated as Jared and Darkhax
            else if (param.equalsIgnoreCase("jarhax")) {

                authors.add("darkh4x");
                authors.add("jaredlll08");
            }

            // Darkhax has a messed up curse account that has multiple names. Remap to the
            // right one.
            else if (param.equalsIgnoreCase("darkhax") || param.equals("darkhaxdev")) {

                authors.add("darkh4x");
            }

            else if (param.equals("mmd")) {

                authors.addAll(Arrays.asList(new String[] { "darkh4x", "jriwanek", "jamieswhiteshirt", "dshadowwolf", "lordcazsius", "morpheus1101", "poke1650", "proxyneko", "jaredlll08", "lclc98", "trentv4", "cleverpanda714", "pau101", "lewis_mcreu", "racerdelux", "boqzo1", "zaweri", "carstorm", "minecraftmoddevelopment", "spicephantom" }));
            }

            // Remaps the lemon emoji to lemonszz
            else if (param.codePointAt(0) == 127819) {

                authors.add("lemonszz");
            }
            else if(param.equalsIgnoreCase("vazkey") || param.equalsIgnoreCase("vazkee") || param.equalsIgnoreCase("vazkoo")){
                authors.add("vazkii");
            }

            // The name * is treated as all mod devs.
            else if (param.equalsIgnoreCase("*")) {

//                authors.addAll(CurseMetaTracker.instance.getAuthors().keySet());
            }
//            else if(param.equalsIgnoreCase("jaredlll08")){
//                MessageUtils.sendMessage(channel, "I think you spelt mcjty wrong");
//                authors.add("mcjty");
//            }

            // The name has no overrides.
            else {

                authors.add(param.toLowerCase());
            }
        }
    
        bot.sendMessage(channel, new MessageMods(10, authors.toArray(new String[0])).build());
    }

    @Override
    public String getDescription () {

        return "Prints out information about mod authors on Curse. You can list multiple author names, or use * for all mod authors.";
    }
}