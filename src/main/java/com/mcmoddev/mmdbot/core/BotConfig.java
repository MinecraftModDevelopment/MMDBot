package com.mcmoddev.mmdbot.core;

/**
 *
 */
@Deprecated
public final class BotConfig {

    /**
     * Discord Bot Token.
     */
    private String botToken = "Enter your bot token here.";

    /**
     * The ID of the bots owner. (Currently Proxy)
     */
    private String ownerID = "141990014346199040";

    /**
     * The bots Command Prefix.
     */
    private String prefix = "!mmd-";

    /**
     * The alternative command prefix, shorter and faster to type out than the long one.
     */
    private String alternativePrefix = "!";

    /**
     * The ID of the guild we will be running the bot in.
     */
    private Long guildID = 0L;

    /**
     * The channel the bots commands are primarily used in.
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
     * ID for Deleted Messages Channel.
     */
    private Long channelIDDeletedMessages = 0L;

    /**
     * ID for the Readme Channel.
     */
    private Long channelIDReadme = 0L;

    /**
     * ID for the Rules channel.
     */
    private Long channelIDRules = 0L;

    /**
     * ID for the bots Debug Channel.
     */
    private Long channelIDDebug = 0L;

    /**
     * ID for the bots Console Channel.
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
     * The channel ID of the channel we want to put forge updates notifications in.
     */
    private Long channelIDForgeNotifier = 0L;

    /**
     * The Staff role ID.
     */
    private String roleStaff = "218607518048452610";

    /**
     * The Community Reps role ID.
     */
    private String roleCommunityRep = "286223615765118986";

    /**
     * The Modder role ID.
     */
    private String roleModder = "191145754583105536";

    /**
     * The Artist role ID.
     */
    private String roleArtist = "179305517343047680";

    /**
     * The Streamer role ID.
     */
    private String roleStreamer = "219679192462131210";

    /**
     * The Modpack Maker role ID.
     */
    private String roleModpackMaker = "215403201090813952";

    /**
     * The Translator role ID.
     */
    private String roleTranslator = "201471697482678273";

    /**
     * The Event Notifications role ID.
     */
    private String roleEventNotifications = "777633879938826252";

    /**
     * The Booster role ID.
     */
    private String roleBooster = "590166091234279465";

    /**
     * The Public Server Player role ID.
     */
    private String rolePublicServerPlayer = "325780906579066881";

    /**
     * The Muted role ID.
     */
    private String roleMuted = "305875306529554432";

    /**
     * The ID of the request is bad emoticon.
     */
    private Long[] emoteIDsBad = new Long[]{0L};

    /**
     * The ID of the request needs improvement emoticon.
     */
    private Long[] emoteIDsNeedsImprovement = new Long[]{0L};

    /**
     * The ID of the request is good emoticon.
     */
    private Long[] emoteIDsGood = new Long[]{0L};

    /**
     * The bad reaction threshold.
     */
    private double badReactionThreshold = 0.0;

    /**
     * The bad reaction warning threshold.
     */
    private double warningBadReactionThreshold = 0.0;

    /**
     *
     */
    public BotConfig() {
        //
    }

    /**
     * @return The bots token.
     */
    public String getBotToken() {
        return botToken;
    }

    /**
     * @return The ID of the bot owner.
     */
    public String getOwnerID() {
        return ownerID;
    }

    /**
     * @return The prefix the bot will use when users run commands.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return The alternative prefix the bot will use when users run commands.
     */
    public String getAlternativePrefix() {
        return alternativePrefix;
    }

    /**
     * @return The guild/servers ID.
     */
    public Long getGuildID() {
        return guildID;
    }

    /**
     * @return The channel ID for the bot-stuff channel.
     */
    public Long getBotStuffChannelId() {
        return botStuffChannelId;
    }

    /**
     * @return The channel ID for the basic events channel.
     */
    public Long getChannelIDBasicEvents() {
        return channelIDBasicEvents;
    }

    /**
     * @return The channel ID for the important events channel.
     */
    public Long getChannelIDImportantEvents() {
        return channelIDImportantEvents;
    }

    /**
     * @return The channel ID of the channel the bot logs deleted messages in.
     */
    public Long getChannelIDDeletedMessages() {
        return channelIDDeletedMessages;
    }

    /**
     * @return The channel ID of the bots debug channel.
     */
    public Long getChannelIDDebug() {
        return channelIDDebug;
    }

    /**
     * @return The channel ID for the bots console channel.
     */
    public Long getChannelIDConsole() {
        return channelIDConsole;
    }

    /**
     * @return The channel ID for the readme channel.
     */
    public Long getChannelIDReadme() {
        return channelIDReadme;
    }

    /**
     * @return The channel ID for the rules.
     */
    public Long getChannelIDRules() {
        return channelIDRules;
    }

    /**
     * @return The channel ID for requests channel.
     */
    public Long getChannelIDRequests() {
        return channelIDRequests;
    }

    /**
     * @return The channel ID for request-discussion.
     */
    public Long getChannelIDRequestsDiscussion() {
        return channelIDRequestsDiscussion;
    }

    /**
     * @return The channel ID of the channel we want to display forge update notifications in.
     */
    public Long getChannelIDForgeNotifier() {
        return channelIDForgeNotifier;
    }

    /**
     * @return The role ID of the Staff role.
     */
    public String getRoleStaff() {
        return roleStaff;
    }

    /**
     * @return The role ID of the Community Reps role.
     */
    public String getRoleCommunityRep() {
        return roleCommunityRep;
    }

    /**
     * @return The role ID of the Modder role.
     */
    public String getRoleModder() {
        return roleModder;
    }

    /**
     * @return The role ID of the Artist role.
     */
    public String getRoleArtist() {
        return roleArtist;
    }

    /**
     * @return The role ID of the Streamer role.
     */
    public String getRoleStreamer() {
        return roleStreamer;
    }

    /**
     * @return The role ID of the Modpack Maker role.
     */
    public String getRoleModpackMaker() {
        return roleModpackMaker;
    }

    /**
     * @return The ID of the Translator role.
     */
    public String getRoleTranslator() {
        return roleTranslator;
    }

    /**
     * @return The ID of the Event Notifications role.
     */
    public String getRoleEventNotifications() {
        return roleEventNotifications;
    }

    /**
     * @return The ID of the Booster role.
     */
    public String getRoleBooster() {
        return roleBooster;
    }

    /**
     * @return The ID of the public server player role.
     */
    public String getRolePublicServerPlayer() {
        return rolePublicServerPlayer;
    }

    /**
     * @return The ID of the muted role.
     */
    public String getRoleMuted() {
        return roleMuted;
    }

    /**
     * @return The ID of the request is bad emoticon.
     */
    public Long[] getEmoteIDsBad() {
        return emoteIDsBad;
    }

    /**
     * @return The ID of the request needs improvement emoticon.
     */
    public Long[] getEmoteIDsNeedsImprovement() {
        return emoteIDsNeedsImprovement;
    }

    /**
     * @return The ID of the request is good emoticon.
     */
    public Long[] getEmoteIDsGood() {
        return emoteIDsGood;
    }

    /**
     * @return The threshold of the request is bad emoticon.
     */
    public double getBadReactionThreshold() {
        return badReactionThreshold;
    }

    /**
     * @return The warning threshold of the request is bad emoticon.
     */
    public double getWarningBadReactionThreshold() {
        return warningBadReactionThreshold;
    }
}
