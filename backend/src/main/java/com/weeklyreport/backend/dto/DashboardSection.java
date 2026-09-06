package com.weeklyreport.backend.dto;

import com.weeklyreport.backend.exception.UnknownSectionException;
import java.util.Locale;

/** Which report section the side-by-side comparison shows. */
public enum DashboardSection {
    BLOCKERS,
    ACHIEVEMENTS;

    /** Accepts the {@code blockers} / {@code achievements} query values, case-insensitively. */
    public static DashboardSection parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException notASection) {
            throw new UnknownSectionException(raw);
        }
    }
}
