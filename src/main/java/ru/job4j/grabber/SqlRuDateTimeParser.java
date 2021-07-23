package ru.job4j.grabber;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlRuDateTimeParser implements DateTimeParser {
    private static final Map<String, Integer> MONTHS = Map.ofEntries(
            Map.entry("янв", 1),
            Map.entry("фев", 2),
            Map.entry("мар", 3),
            Map.entry("апр", 4),
            Map.entry("май", 5),
            Map.entry("июн", 6),
            Map.entry("июл", 7),
            Map.entry("авг", 8),
            Map.entry("сен", 9),
            Map.entry("окт", 10),
            Map.entry("ноя", 11),
            Map.entry("дек", 12)
    );

    @Override
    public LocalDateTime parse(String parse) {
        String[] dates = parse.split(", ");
        int month;
        Pattern pattern = Pattern.compile("\\D++");
        Matcher matcher = pattern.matcher(dates[0]);
        LocalDate localDate = LocalDate.now().minusDays(1);
        LocalTime localTime = LocalTime.parse(dates[1]);
         if (matcher.find()) {
             if (matcher.group().equals("сегодня")) {
                 localDate = LocalDate.now();
             }
         } else {
             String[] datePlusYear = dates[0].split("\\s");
             month = getMonth(datePlusYear[1]);
             localDate = LocalDate.of(Integer.parseInt(datePlusYear[2]),
                  month, Integer.parseInt(datePlusYear[0]));
         }
         return LocalDateTime.of(localDate, localTime);
    }

    private static int getMonth(String month) {
        return MONTHS.get(month);
    }

    public static void main(String[] args) {
        SqlRuDateTimeParser sq = new SqlRuDateTimeParser();
        String rsl = "вчера, 19:30";
        System.out.println(sq.parse(rsl));
    }
}