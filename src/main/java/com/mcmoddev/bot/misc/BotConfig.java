package com.mcmoddev.bot.misc;

/**
 *
 */
public final class BotConfig {

    /**
     * Discord Bot Token.
     */
    private String botToken = "Enter your bot token here.";

    /**
     *
     */
    private String ownerID =  "141990014346199040";

    /**
     * Bot Command Prefix.
     */
    private String prefix = "Command prefix";

    /**
     *
     */
    private String botTextStatus = "Can be \'I'm watching you\' or something similar but not too long.";
    /**
     * Bot Status Message.
     */
    private Long guildID = 0L;

    /**
     * Guild ID.
     */
    private Long botStuffChannelId = 0L;

    //Channel ID's
    /**
     * ID for Basic Events Channel.
     */
    private Long channelIDBasicEvents = 0L;

    /**
     * ID for Important Events Channel.
     */
    private Long channelIDImportantEvents = 0L;

    /**
     * ID for Deleted Messages Chanmel.
     */
    private Long channelIDDeletedMessages = 0L;

    /**
     * ID for Readme Channel
     */
    private Long channelIDReadme = 0L;

    /**
     * ID for Debug Channel.
     */
    private Long channelIDDebug = 0L;

    /**
     * ID for Console Channel.
     */
    private Long channelIDConsole = 0L;

    /**
     * ID for Requests Channel
     */
    private Long channelIDRequests = 0L;

    /**
     * ID for Requests Channel
     */
    private Long channelIDRequestsDiscussion = 0L;

    /**
     *
     */
    private String roleStaff = "218607518048452610";

    /**
     *
     */
    private String rolePartner = "252880821382283266";

    /**
     *
     */
    private String roleCommunityRep = "286223615765118986";

    /**
     *
     */
    private String roleModder = "191145754583105536";

    /**
     *
     */
    private String roleArtist = "179305517343047680";

    /**
     *
     */
    private String roleStreamer = "219679192462131210";

    /**
     *
     */
    private String roleModpackMaker = "215403201090813952";

    /**
     *
     */
    private String roleTranslator = "201471697482678273";

    /**
     *
     */
    private String roleBooster = "590166091234279465";

    /**
     *
     */
    private String rolePublicServerPlayer = "325780906579066881";

    /**
     *
     */
    private Long[] emoteIDsBad = new Long[] { 0L };

    /**
     *
     */
    private Long[] emoteIDsNeedsImprovement = new Long[] { 0L };

    /**
     *
     */
    private Long[] emoteIDsGood = new Long[] { 0L };

    /**
     *
     */
    private double badReactionThreshold = 0.0;

    /**
     *
     */
    private double warningBadReactionThreshold = 0.0;

    /**
	 *
	 */
	public BotConfig() {
		//
	}

	/**
     *
     * @return
     */
    public String getBotToken() {
        return botToken;
    }

    /**
     *
     * @return
     */
    public String getOwnerID() {
        return ownerID;
    }

    /**
     *
     * @return
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     *
     * @return
     */
    public String getBotTextStatus() {
        return botTextStatus;
    }

    /**
     *
     * @return
     */
    public Long getGuildID() {
        return guildID;
    }

    /**
     *
     * @return
     */
    public Long getBotStuffChannelId() {
        return botStuffChannelId;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDBasicEvents() {
        return channelIDBasicEvents;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDImportantEvents() {
        return channelIDImportantEvents;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDDeletedMessages() {
        return channelIDDeletedMessages;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDDebug() {
        return channelIDDebug;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDConsole() {
        return channelIDConsole;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDReadme() {
        return channelIDReadme;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDRequests() {
        return channelIDRequests;
    }

    /**
     *
     * @return
     */
    public Long getChannelIDRequestsDiscussion() {
        return channelIDRequestsDiscussion;
    }

    /**
     *
     * @return
     */
    public String getRoleStaff() {
    	return roleStaff;
    }

    /**
     *
     * @return
     */
    public String getRolePartner() {
    	return rolePartner;
    }

    /**
     *
     * @return
     */
    public String getRoleCommunityRep() {
    	return roleCommunityRep;
    }

    /**
     *
     * @return
     */
    public String getRoleModder() {
    	return roleModder;
    }

    /**
     *
     * @return
     */
    public String getRoleArtist() {
    	return roleArtist;
    }

    /**
     *
     * @return
     */
    public String getRoleStreamer() {
    	return roleStreamer;
    }

    /**
     *
     * @return
     */
    public String getRoleModpackMaker() {
    	return roleModpackMaker;
    }

    /**
     *
     * @return
     */
    public String getRoleTranslator() {
    	return roleTranslator;
    }

    /**
     *
     * @return
     */
    public String getRoleBooster() {
    	return roleBooster;
    }

    /**
     *
     * @return
     */
    public String getRolePublicServerPlayer() {
        return rolePublicServerPlayer;
    }

    /**
     *
     * @return
     */
    public Long[] getEmoteIDsBad() {
        return emoteIDsBad;
    }

    /**
     *
     * @return
     */
    public Long[] getEmoteIDsNeedsImprovement() {
        return emoteIDsNeedsImprovement;
    }

    /**
     *
     * @return
     */
    public Long[] getEmoteIDsGood() {
        return emoteIDsGood;
    }

    /**
     *
     * @return
     */
    public double getBadReactionThreshold() {
        return badReactionThreshold;
    }

    /**
     *
     * @return
     */
    public double getWarningBadReactionThreshold() {
        return warningBadReactionThreshold;
    }
}
