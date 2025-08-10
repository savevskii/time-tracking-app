package com.fsavevsk.timetracking.util;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

public final class DateRanges {

    private DateRanges() {}

    /* ===== Today ===== */
    public static LocalDateTime startOfToday(ZoneId zone, Clock clock) {
        return LocalDate.now(clock.withZone(zone)).atStartOfDay();
    }
    public static LocalDateTime endOfToday(ZoneId zone, Clock clock) {
        return startOfToday(zone, clock).plusDays(1);
    }

    /* ===== Week (Mon..next Mon) ===== */
    public static LocalDateTime startOfWeek(ZoneId zone, Clock clock) {
        var today = LocalDate.now(clock.withZone(zone));
        var monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return monday.atStartOfDay();
    }

    public static LocalDateTime endOfWeek(ZoneId zone, Clock clock) {
        return startOfWeek(zone, clock).plusWeeks(1);
    }

    /* ===== Month (1st..next 1st) ===== */
    public static LocalDateTime startOfMonth(ZoneId zone, Clock clock) {
        var today = LocalDate.now(clock.withZone(zone));
        var first = today.with(TemporalAdjusters.firstDayOfMonth());
        return first.atStartOfDay();
    }

    public static LocalDateTime endOfMonth(ZoneId zone, Clock clock) {
        return startOfMonth(zone, clock).plusMonths(1);
    }

    /* ===== Parsing helpers ===== */
    /** If iso is null/blank, returns fallback; else parses ISO-8601 LocalDateTime. */
    public static LocalDateTime parseOr(LocalDateTime fallback, String iso) {
        if (iso == null || iso.isBlank()) return fallback;
        return LocalDateTime.parse(iso);
    }
}
