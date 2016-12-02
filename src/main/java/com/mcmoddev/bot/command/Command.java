package com.mcmoddev.bot.command;

import sx.blah.discord.handle.obj.IMessage;

public interface Command {
	
	/**
	 * Called when the command is executed. Commands will not be executed if they fail the {@link #isValidUsage(IMessage)} check.
	 * @param message The context of the message received. Includes the guild, sender, message contents, and more.
	 * @param params The individual parameter messages. 
	 */
    public void proccessCommand (IMessage message, String[] params);
    
    /**
     * Provides a description for the command. This is used by the help command to tell people what the command does, and explains how to use it.
     * @return The description for the command.
     */
    public String getDescription ();
    
    /**
     * Checks if a command is valid. If not, it will not be executed.
     * 
     * @param message The message which contains all the command information.
     * @return boolean Whether or not the command should execute.
     */
    
    /**
     * Checks if the message is valid for the command. This is intended for use with player permissions, but can also be used to allow other misc checks.
     * @param message The context of the message recieved. Includes the guild, sender, message contents, and more.
     * @return
     */
    default public boolean isValidUsage (IMessage message) {
        
        return true;
    }
}
