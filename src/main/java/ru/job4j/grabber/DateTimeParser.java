package ru.job4j.grabber;

import java.time.LocalDateTime;

public interface DateTimeParser {
    LocalDateTime parse(String parse);
}