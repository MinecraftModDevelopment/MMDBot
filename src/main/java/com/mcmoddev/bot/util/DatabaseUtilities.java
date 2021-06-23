package com.mcmoddev.bot.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseUtilities {

    public static String getDate(LocalDateTime ldt) {
        return ldt.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static Timestamp getDateTime(LocalDateTime ldt) {
        return Timestamp.valueOf(ldt);
    }
}
