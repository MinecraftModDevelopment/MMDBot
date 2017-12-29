package net.darkhax.botbase.commands.mcp;

import net.darkhax.botbase.IDiscordBot;
import net.darkhax.botbase.commands.Command;
import net.darkhax.botbase.commands.mcp.MCPData.MappingData;
import net.darkhax.botbase.utils.MessageUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class CommandMCP implements Command {

    @Override
    public void processCommand (IDiscordBot bot, IChannel channel, IMessage message, String[] params) {

        if (params.length == 1) {

            if (!MCPData.FIELDS.containsKey(params[0])) {

                MessageUtils.sendMessage(channel, "Could not find entry: " + params[0]);
            }

            for (final MappingData data : MCPData.FIELDS.get(params[0])) {

                MessageUtils.sendMessage(channel, new MessageMCP(data).build());
            }
        }
    }

    @Override
    public String getDescription () {

        // TODO Auto-generated method stub
        return null;
    }

}
