package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Metodi di utility per la conversione tra date. LocalDate --> java.sql.Date
 * LocalDateTime --> java.sql.DateTime
 */
public class DateConverter {

    public static LocalDate date2LocalDate(Date date2convert) {
        return date2convert.toLocalDate();
    }

    public static LocalDateTime convertLocalDateTimeFromTimestamp(Timestamp ts) {
        return ts.toLocalDateTime();
    }

    public static Timestamp convertTimestampFromLocalDateTime(LocalDateTime ldt) {
        return Timestamp.valueOf(ldt);
    }

    public static Date toSqlDate(LocalDate localDate) {
        return Date.valueOf(localDate);
    }

}