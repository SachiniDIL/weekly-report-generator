package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.domain.ReportStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository
        extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    long countByStatus(ReportStatus status);

    List<Report> findByWeekStartAndWeekEnd(LocalDate weekStart, LocalDate weekEnd);

    /** Reports whose week contains {@code today} and that have moved past DRAFT. */
    long countByStatusNotAndWeekStartLessThanEqualAndWeekEndGreaterThanEqual(
            ReportStatus status, LocalDate weekStart, LocalDate weekEnd);

    /** Reports whose reporting week has started and that have moved past DRAFT. */
    long countByStatusNotAndWeekStartLessThanEqual(ReportStatus status, LocalDate weekStart);

    /** DRAFT reports whose week has already ended. */
    long countByStatusAndWeekEndLessThan(ReportStatus status, LocalDate weekEnd);
}
