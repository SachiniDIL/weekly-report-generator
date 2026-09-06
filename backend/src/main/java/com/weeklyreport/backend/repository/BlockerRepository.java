package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Blocker;
import com.weeklyreport.backend.domain.ReportStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockerRepository extends JpaRepository<Blocker, Long> {

    List<Blocker> findByReportVersionId(Long reportVersionId);

    void deleteByReportVersionId(Long reportVersionId);

    /** Current-version blockers for a week, skipping reports still in {@code excludedStatus} (DRAFT). */
    @Query(
            """
            select r.user.id as userId, b.description as description, b.keyIssue as keyItem
            from ReportVersion rv
              join rv.report r
              join Blocker b on b.reportVersionId = rv.id
            where rv.versionNo = r.currentVersionNo
              and r.status <> :excludedStatus
              and r.weekStart = :weekStart and r.weekEnd = :weekEnd
            order by b.id
            """)
    List<SectionItemProjection> findCurrentVersionBlockersForWeek(
            @Param("excludedStatus") ReportStatus excludedStatus,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);

    /**
     * Blockers on the current version of every report not in {@code excludedStatus} — a
     * corrected report's older blockers are excluded because they may no longer apply.
     */
    @Query(
            """
            select count(b) from Blocker b
            where b.reportVersionId in (
                select rv.id from ReportVersion rv
                where rv.report.status <> :excludedStatus
                  and rv.versionNo = rv.report.currentVersionNo
            )
            """)
    long countOnCurrentVersionsOfReportsNotInStatus(@Param("excludedStatus") ReportStatus excludedStatus);
}
