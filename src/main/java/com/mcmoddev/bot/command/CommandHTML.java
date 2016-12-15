package com.mcmoddev.bot.command;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import sx.blah.discord.handle.obj.IMessage;

public class CommandHTML extends CommandAdmin {
    
    @Override
    public void processCommand (IMessage message, String[] params) {
        
        if (params.length == 2)
            try (InputStream stream = new URL(String.format("https://minecraft.curseforge.com/members/%s/projects?page=1", params[1])).openStream()) {
                
                final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                
                for (String line = reader.readLine(); line != null; line = reader.readLine())
                    System.out.println(line);
                
            }
            
            catch (final Exception e) {
                
                e.printStackTrace();
            }
    }
    
    @Override
    public String getDescription () {
        
        return "Dumps an html page as plain text... for reasons.";
    }
}
