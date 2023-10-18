package org.example.utils;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Optional;

public final class DateUtils {

    public static Optional<Instant> isValidInstant(String value) {
        try {
            return Optional.of(
                  Instant.ofEpochSecond(
                        LocalDateTime.parse(value, DATE_TIME_FORMATTER).toEpochSecond(ZoneOffset.UTC)));
        } catch (DateTimeException ex) {
            return Optional.empty();
        }
    }

    private static final ZoneId ZONE_ID_UTC = ZoneId.of("UTC");

    // uuuu is required by ResolverStyle.STRICT instead of yyyy
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
          DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")
                .withResolverStyle(ResolverStyle.STRICT)
                .withZone(ZONE_ID_UTC);
}
