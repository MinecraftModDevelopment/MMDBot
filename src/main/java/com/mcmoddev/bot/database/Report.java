package com.mcmoddev.bot.database;

import java.sql.Timestamp;

public class Report {

    private final int id;
    private final String userID;
    private final String desc;
    private final Timestamp timestamp;
    private final int points;
    private final String reporter;
    private final boolean expired;

    public Report(int id, String userID, String desc, Timestamp timestamp, int points, String reporter, boolean expired) {
        this.id = id;
        this.userID = userID;
        this.desc = desc;
        this.timestamp = timestamp;
        this.points = points;
        this.reporter = reporter;
        this.expired = expired;
    }

    public int getId() {
        return id;
    }

    public String getUserID() {
        return userID;
    }

    public String getDesc() {
        return desc;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getPoints() {
        return points;
    }

    public String getReporter() {
        return reporter;
    }

    public boolean isExpired() {
        return expired;
    }
}
