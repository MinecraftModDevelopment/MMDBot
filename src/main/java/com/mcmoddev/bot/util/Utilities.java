package com.mcmoddev.bot.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;
import com.mcmoddev.bot.MMDBot;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Utilities {
    
    /**
     * Static reference to the line seperator on the current operating system.
     */
    public static final String SEPERATOR = System.lineSeparator();
    
    /**
     * A wrapper for {@link FileUtils#copyURLToFile(URL, File)}. Allows for quick download of
     * files based on input from users.
     * 
     * @param site The site/url to download the file from.
     * @param fileName The location to save the file to.
     * @return The file that was downloaded.
     */
    public static File downloadFile (String site, String fileName) {
        
        final File file = new File(fileName);
        
        try {
            
            FileUtils.copyURLToFile(new URL(site), file);
        }
        
        catch (final IOException e) {
            
            e.printStackTrace();
        }
        
        return file;
    }
    
    /**
     * Creates a ping message for a user based upon their user ID.
     * 
     * @param userID The user ID of the user to generate a ping message for.
     * @return String A short string which will ping the specified user when sent into the
     *         chat.
     */
    public static String getPingMessage (String userID) {
        
        return "<@" + userID + ">";
    }
    
    /**
     * Makes a String message italicized. This only applies to chat.
     * 
     * @param message The message to format.
     * @return String The message with the formatting codes applied.
     */
    public static String makeItalic (String message) {
        
        return "*" + message + "*";
    }
    
    /**
     * Makes a String message bold. This only applies to chat.
     * 
     * @param message The message to format.
     * @return String The message with the bold formatting codes applied.
     */
    public static String makeBold (String message) {
        
        return "**" + message + "**";
    }
    
    /**
     * Makes a String message scratched out. This only applies to chat.
     * 
     * @param message The message to format.
     * @return String The message with the scratched out formatting codes applied.
     */
    public static String makeScratched (String message) {
        
        return "~~" + message + "~~";
    }
    
    /**
     * Makes a String message underlined. This only applies to chat.
     * 
     * @param message The message to format.
     * @return String The message with the underlined formatting codes applied.
     */
    public static String makeUnderlined (String message) {
        
        return "__" + message + "__";
    }
    
    /**
     * Makes a String message appear in a code block. This only applies to chat.
     * 
     * @param message The message to format.
     * @return String The message with the code block format codes applied.
     */
    public static String makeCodeBlock (String message) {
        
        return "`" + message + "`";
    }
    
    /**
     * Makes a string which represents multiple lines of text.
     * 
     * @param lines The lines of text to display. Each entry is a new line.
     * @return A string which has been split up.
     */
    public static String makeMultilineMessage (String... lines) {
        
        String text = "";
        
        for (final String line : lines)
            text += line + SEPERATOR;
        
        return text;
    }
    
    /**
     * Makes a String message appear in a multi-lined code block. This only applies to chat.
     * 
     * @param message The message to format.
     * @return String The message with the multi-lined code block format codes applied.
     */
    public static String makeMultiCodeBlock (String message) {
        
        return "```" + message + "```";
    }
    
    /**
     * Attempts to send a private message to a user. If a private message channel does not
     * already exist, it will be created.
     * 
     * @param instance An instance of your IDiscordClient.
     * @param user The user to send the private message to.
     * @param message The message to send to the user.
     */
    public static void sendPrivateMessage (IUser user, String message) {
        
        try {
            
            sendMessage(MMDBot.instance.getOrCreatePMChannel(user), message, false);
        }
        
        catch (final Exception e) {
            
            e.printStackTrace();
        }
    }
    
    public static void sendMessage (IChannel channel, String message, EmbedObject object) {
        
        try {
            
            channel.sendMessage(message, object, false);
            Thread.sleep(1000);
        }
        
        catch (RateLimitException | DiscordException | MissingPermissionsException | InterruptedException e) {
            
            e.printStackTrace();
        }
    }
    
    /**
     * Sends a message into the chat. This version of the method will handle exceptions for
     * you.
     * 
     * @param channel The channel to send to message to.
     * @param message The message to send to the channel.
     */
    public static void sendMessage (IChannel channel, String message) {
        
        sendMessage(channel, message, true);
    }
    
    public static void sendMessage (IChannel channel, String message, boolean timeout) {
        
        if (message.contains("@everyone") || message.contains("@here")) {
            
            Utilities.sendMessage(channel, "I refuse to ping everyone!", timeout);
            return;
        }
        
        try {
            
            channel.sendMessage(message);
            
            if (timeout)
                Thread.sleep(1000);
        }
        
        catch (MissingPermissionsException | DiscordException | RateLimitException | InterruptedException e) {
            
            if (e instanceof DiscordException && e.toString().contains("String value is too long"))
                sendMessage(channel, "I tried to send a message, but it was too long.", timeout);
            
            if (e instanceof NullPointerException)
                MMDBot.LOG.info("Attempted to send message to null channel.");
            
            else
                e.printStackTrace();
        }
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue (Map<K, V> map, boolean invert) {
        
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare (Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        
        if (invert)
            list = Lists.reverse(list);
        
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}