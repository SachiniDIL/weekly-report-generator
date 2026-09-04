package com.weeklyreport.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** One planned/actual task line within a {@link ReportVersion} snapshot. */
@Entity
@Table(name = "task_entries")
@Getter
@Setter
public class TaskEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_version_id", nullable = false)
    private Long reportVersionId;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(nullable = false)
    private String priority;

    @Column(name = "planned_pct", nullable = false)
    private int plannedPct;

    @Column(name = "actual_pct", nullable = false)
    private int actualPct;

    @Column(nullable = false)
    private String status;

    @Column(name = "time_planned")
    private Integer timePlanned;

    @Column(name = "time_spent")
    private Integer timeSpent;

    private String deliverable;
}
