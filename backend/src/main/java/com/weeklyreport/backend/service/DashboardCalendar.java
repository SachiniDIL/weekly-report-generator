package com.weeklyreport.backend.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import org.springframework.stereotype.Component;

/** The dashboard's shared notion of "today" and "which week" — one place so every endpoint agrees. */
@Component
class DashboardCalendar {

    private final Clock clock;

    DashboardCalendar(Clock clock) {
        this.clock = clock;
    }

    LocalDate today() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    /** The caller's {@code [weekStart, weekEnd]} if both are given, else the ISO week containing today. */
    WeekRange resolveWeek(LocalDate weekStart, LocalDate weekEnd) {
        if (weekStart != null && weekEnd != null) {
            return new WeekRange(weekStart, weekEnd);
        }
        LocalDate monday = today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new WeekRange(monday, monday.plusDays(6));
    }

    record WeekRange(LocalDate start, LocalDate end) {}
}
