package com.mcmoddev.bot.command.moderative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.darkhax.botbase.BotBase;
import net.darkhax.botbase.commands.CommandAdmin;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandBackup extends CommandAdmin {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    
    @Override
    public void processCommand (BotBase bot, IChannel channel, IMessage message, String[] params) {
        
        try {
            
            final File file = new File(String.format("backups/%s.zip", TIME_FORMAT.format(message.getTimestamp())));
            
            if (!file.getParentFile().exists()) {
                
                file.getParentFile().mkdirs();
            }
            final ZipFile zipFile = new ZipFile(file);
            final ZipParameters zipParams = new ZipParameters();
            zipParams.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParams.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
            zipParams.setEncryptFiles(true);
            zipParams.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
            zipParams.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
            zipParams.setPassword(new char[] {'p', 'a', 's', 's', 'w', 'o', 'r', 'd'});
            zipFile.createZipFileFromFolder("data", zipParams, false, 0);
            zipFile.addFile(new File("data"), zipParams);
            
            channel.sendFile(file);
        }
        
        catch (ZipException | FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription () {
        
        return "Uploads a password protected archive containing all of the data from the bot.";
    }
}