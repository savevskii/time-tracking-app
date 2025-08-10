package com.fsavevsk.timetracking.util;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangesTest {

    private static final ZoneId SKOPJE = ZoneId.of("Europe/Skopje");

    /** 2025-08-10 is a Sunday. We'll freeze time at 2025-08-10T13:45:00+02:00 (Skopje in summer). */
    private Clock fixedSkopjeClock() {
        // build an instant that corresponds to 2025-08-10T13:45 in Skopje (+02:00)
        var local = LocalDateTime.of(2025, 8, 10, 13, 45);
        var zoned = local.atZone(SKOPJE);
        return Clock.fixed(zoned.toInstant(), ZoneOffset.UTC); // store as UTC internally; we always pass zone explicitly
    }

    @Test
    void today_range_is_midnight_to_next_midnight_in_zone() {
        var clock = fixedSkopjeClock();

        var start = DateRanges.startOfToday(SKOPJE, clock);
        var end   = DateRanges.endOfToday(SKOPJE, clock);

        assertThat(start).isEqualTo(LocalDateTime.of(2025, 8, 10, 0, 0));
        assertThat(end).isEqualTo(LocalDateTime.of(2025, 8, 11, 0, 0));
    }

    @Test
    void week_range_monday_to_next_monday_in_zone() {
        var clock = fixedSkopjeClock();

        var start = DateRanges.startOfWeek(SKOPJE, clock);
        var end   = DateRanges.endOfWeek(SKOPJE, clock);

        // Sun Aug 10, 2025 → previousOrSame(MONDAY) is Mon Aug 04, 2025
        assertThat(start).isEqualTo(LocalDateTime.of(2025, 8, 4, 0, 0));
        assertThat(end).isEqualTo(LocalDateTime.of(2025, 8, 11, 0, 0));
    }

    @Test
    void month_range_first_day_to_next_first_day_in_zone() {
        var clock = fixedSkopjeClock();

        var start = DateRanges.startOfMonth(SKOPJE, clock);
        var end   = DateRanges.endOfMonth(SKOPJE, clock);

        assertThat(start).isEqualTo(LocalDateTime.of(2025, 8, 1, 0, 0));
        assertThat(end).isEqualTo(LocalDateTime.of(2025, 9, 1, 0, 0));
    }

    @Test
    void parseOr_returns_fallback_when_blank_or_null_otherwise_parses_iso() {
        var fb = LocalDateTime.of(2025, 1, 1, 0, 0);

        assertThat(DateRanges.parseOr(fb, null)).isEqualTo(fb);
        assertThat(DateRanges.parseOr(fb, "   ")).isEqualTo(fb);
        assertThat(DateRanges.parseOr(fb, "2025-08-10T16:30"))
                .isEqualTo(LocalDateTime.of(2025, 8, 10, 16, 30));
    }
}
