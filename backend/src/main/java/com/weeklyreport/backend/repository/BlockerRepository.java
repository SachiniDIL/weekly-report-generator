package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Blocker;
import com.weeklyreport.backend.domain.ReportStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockerRepository extends JpaRepository<Blocker, Long> {

    List<Blocker> findByReportVersionId(Long reportVersionId);

    void deleteByReportVersionId(Long reportVersionId);

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
