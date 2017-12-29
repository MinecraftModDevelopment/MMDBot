package net.darkhax.botbase.commands.mcp;

import net.darkhax.botbase.commands.mcp.MCPData.MappingData;
import sx.blah.discord.util.EmbedBuilder;

public class MessageMCP extends EmbedBuilder {

    public MessageMCP (MappingData data) {

        super();
        this.setLenient(true);

        this.appendField("MCP Name", data.getNameMcp(), true);
        this.appendField("Srg Name", data.getNameSearge(), true);
        this.appendField("Side", data.getSide(), true);
        this.appendField("Description", data.getDescription(), false);
    }
}