package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Achievement;
import com.weeklyreport.backend.domain.ReportStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByReportVersionId(Long reportVersionId);

    void deleteByReportVersionId(Long reportVersionId);

    /** Current-version achievements for a week, skipping reports still in {@code excludedStatus} (DRAFT). */
    @Query(
            """
            select r.user.id as userId, a.description as description, a.keyHighlight as keyItem
            from ReportVersion rv
              join rv.report r
              join Achievement a on a.reportVersionId = rv.id
            where rv.versionNo = r.currentVersionNo
              and r.status <> :excludedStatus
              and r.weekStart = :weekStart and r.weekEnd = :weekEnd
            order by a.id
            """)
    List<SectionItemProjection> findCurrentVersionAchievementsForWeek(
            @Param("excludedStatus") ReportStatus excludedStatus,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd);
}
