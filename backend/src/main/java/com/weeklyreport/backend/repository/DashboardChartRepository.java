package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Report;
import com.weeklyreport.backend.dto.ProjectWorkloadPoint;
import com.weeklyreport.backend.dto.TaskTypeHoursPoint;
import com.weeklyreport.backend.dto.WeeklyTaskCompletionPoint;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Cross-entity aggregations feeding the manager dashboard charts. Every query is scoped to a
 * report's current version — a corrected report's older child rows may no longer apply.
 */
public interface DashboardChartRepository extends Repository<Report, Long> {

    @Query(
            """
            select new com.weeklyreport.backend.dto.WeeklyTaskCompletionPoint(
                r.weekStart, r.weekEnd,
                sum(case when te.actualPct = 100 then 1 else 0 end))
            from ReportVersion rv
              join rv.report r
              left join TaskEntry te on te.reportVersionId = rv.id
            where rv.versionNo = r.currentVersionNo and r.weekStart <= :today
            group by r.weekStart, r.weekEnd
            order by r.weekStart desc
            """)
    List<WeeklyTaskCompletionPoint> tasksCompletedByWeekNewestFirst(@Param("today") LocalDate today);

    @Query(
            """
            select new com.weeklyreport.backend.dto.ProjectWorkloadPoint(r.project.name, count(te))
            from ReportVersion rv
              join rv.report r
              join TaskEntry te on te.reportVersionId = rv.id
            where rv.versionNo = r.currentVersionNo
              and r.weekStart = :weekStart and r.weekEnd = :weekEnd
            group by r.project.name
            """)
    List<ProjectWorkloadPoint> taskCountByProject(
            @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    @Query(
            """
            select new com.weeklyreport.backend.dto.TaskTypeHoursPoint(hb.taskType, sum(hb.hours))
            from ReportVersion rv
              join rv.report r
              join HoursBreakdown hb on hb.reportVersionId = rv.id
            where rv.versionNo = r.currentVersionNo
              and r.weekStart = :weekStart and r.weekEnd = :weekEnd
            group by hb.taskType
            """)
    List<TaskTypeHoursPoint> hoursByTaskType(
            @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);
}
