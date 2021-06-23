package com.mcmoddev.bot.database;


import com.mcmoddev.bot.util.DatabaseUtilities;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

    private Connection connection;

    public void connect() {
        try {
            this.connection = DriverManager.getConnection(String.format("jdbc:%s://%s:%d/%s", "mysql", "127.0.0.1", 3306, "MMD"), "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void disconnect() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /* User Analytics */

    public boolean isExistingUser(IGuild guild, IUser user) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT COUNT(USER_ID) FROM USERS WHERE USER_ID=? AND GUILD_ID=?");
			// TODO: Fix required
            preparedStatement.setString(1, "" /* user.getID() */);
            preparedStatement.setString(2, "" /* guild.getID() */);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
                return resultSet.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void userJoin(IUser user, IGuild guild, LocalDateTime ldt) {
        this.addUserEvent(user, guild, ldt, false);
    }

    public void userLeave(IUser user, IGuild guild, LocalDateTime ldt) {
        this.addUserEvent(user, guild, ldt, true);
    }

    public void addUserEvent(IUser user, IGuild guild, LocalDateTime ldt, boolean leave) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO USERS(USER_ID, GUILD_ID, DAY_TIME, IS_LEAVING) VALUES (?,?,?,?)");
			// TODO: Fix required
            preparedStatement.setString(1, "" /* user.getID() */);
            preparedStatement.setString(2, "" /* guild.getID() */);

            preparedStatement.setTimestamp(3, DatabaseUtilities.getDateTime(ldt));
            preparedStatement.setBoolean(4, leave);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* Server Analytics */

    public void incrementJoinDateCount(LocalDateTime ldt) {
        this.updateDateCount("UPDATE SERVER_ANALYTICS SET USER_JOINED = USER_JOINED + ? WHERE DAY_DATE = ?", ldt, 1);
    }

    public void incrementLeaveDateCount(LocalDateTime ldt) {
        this.updateDateCount("UPDATE SERVER_ANALYTICS SET USER_LEFT = USER_LEFT + ? WHERE DAY_DATE = ?", ldt, 1);
    }

    public void updateDateCount(String SQL, LocalDateTime ldt, int addition) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setInt(1, addition);
            preparedStatement.setTimestamp(2, DatabaseUtilities.getDateTime(ldt));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createOrUpdateJoinLeaveDate() {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT DAY_TIME,SUM(is_leaving = 0) AS 'JOIN', SUM(is_leaving = 1) AS 'LEAVE' FROM USERS GROUP BY DATE(DAY_TIME)");
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Timestamp timestamp = resultSet.getTimestamp(1);
                preparedStatement = this.connection.prepareStatement("INSERT INTO SERVER_ANALYTICS(DAY_DATE, USER_JOINED, USER_LEFT) VALUES (?,?,?)");
                preparedStatement.setString(1, DatabaseUtilities.getDate(timestamp.toLocalDateTime()));
                preparedStatement.setInt(2, resultSet.getInt(2));
                preparedStatement.setInt(3, resultSet.getInt(3));
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* Channel Analytics */

    public boolean isExistingDateChannel(String ID, LocalDateTime ldt) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT COUNT(*) FROM CHANNEL_ANALYTICS WHERE CHANNEL_ID = ? AND DAY_DATE = ?");
            preparedStatement.setString(1, ID);
            preparedStatement.setString(2, DatabaseUtilities.getDate(ldt));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getInt(1) > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void incrementChannelCount(String ID, LocalDateTime ldt) {
        try {
            if (isExistingDateChannel(ID, ldt)) {
                PreparedStatement preparedStatement = this.connection.prepareStatement("UPDATE CHANNEL_ANALYTICS SET MESSAGE_COUNT = MESSAGE_COUNT + 1 WHERE CHANNEL_ID = ? AND DAY_DATE = ?");
                preparedStatement.setString(1, ID);
                preparedStatement.setString(2, DatabaseUtilities.getDate(ldt));
                preparedStatement.executeUpdate();
            } else {
                PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO CHANNEL_ANALYTICS(CHANNEL_ID, DAY_DATE, MESSAGE_COUNT) VALUES (?,?,?)");
                preparedStatement.setString(1, ID);
                preparedStatement.setString(2, DatabaseUtilities.getDate(ldt));
                preparedStatement.setInt(3, 1);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isExistingChannel(IChannel channel) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT COUNT(*) FROM CHANNELS WHERE CHANNEL_ID = ?");
			// TODO: Fix required
            preparedStatement.setString(1, "" /* channel.getID() */);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createOrUpdateChannel(IChannel channel) {
        try {
            PreparedStatement preparedStatement;

            if (isExistingChannel(channel)) {
                preparedStatement = this.connection.prepareStatement("UPDATE CHANNELS SET CHANNEL_NAME = ? WHERE CHANNEL_ID = ?");
            } else {
                preparedStatement = this.connection.prepareStatement("INSERT INTO CHANNELS(CHANNEL_ID, CHANNEL_NAME, GUILD_ID) VALUES (?,?,?)");
				// TODO: Fix required
				preparedStatement.setString(3, "" /* channel.getGuild().getID() */);
            }
			// TODO: Fix required
            preparedStatement.setString(1, "" /*channel.getID()*/);
            preparedStatement.setString(2, channel.getName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getPointCount(String userID, boolean includeExpired) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT SUM(POINTS) FROM USER_REPORTS WHERE USER_ID = ? AND DELETED = FALSE" + (includeExpired ? "" : " AND EXPIRED = FALSE"));
            preparedStatement.setString(1, userID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int createReport(String userID, String points, String desc, LocalDateTime ldt, String reporterID) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("INSERT INTO USER_REPORTS(USER_ID, DESCRIPTION, DAY_TIME, POINTS, REPORTER)  VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, userID);
            preparedStatement.setString(2, desc);
            preparedStatement.setTimestamp(3, DatabaseUtilities.getDateTime(ldt));
            preparedStatement.setString(4, points);
            preparedStatement.setString(5, reporterID);

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next())
                return resultSet.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Report getReport(String ID) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT ID, USER_ID, DESCRIPTION, DAY_TIME, POINTS, REPORTER, EXPIRED FROM USER_REPORTS WHERE ID=?");
            preparedStatement.setString(1, ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                return new Report(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getTimestamp(4), resultSet.getInt(5), resultSet.getString(6), resultSet.getBoolean(2));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Report deleteReport(String ID) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("UPDATE USER_REPORTS SET DELETED = TRUE WHERE ID = ?");
            preparedStatement.setString(1, ID);
            preparedStatement.executeUpdate();
            return getReport(ID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Report[] getReports(IUser user) {
        try {
            List<Report> reports = new ArrayList<>();
            PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT ID, USER_ID, DESCRIPTION, DAY_TIME, POINTS, REPORTER, EXPIRED FROM USER_REPORTS WHERE USER_ID=?");
			// TODO: Fix required
			preparedStatement.setString(1, "" /* user.getID() */);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
                reports.add(new Report(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3), resultSet.getTimestamp(4), resultSet.getInt(5), resultSet.getString(6), resultSet.getBoolean(2)));
            return reports.toArray(new Report[reports.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
