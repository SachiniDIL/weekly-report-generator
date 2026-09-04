package com.weeklyreport.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/** Hours logged against one task type within a {@link ReportVersion} snapshot. */
@Entity
@Table(name = "hours_breakdowns")
@Getter
@Setter
public class HoursBreakdown {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_version_id", nullable = false)
    private Long reportVersionId;

    @Column(name = "task_type", nullable = false)
    private String taskType;

    @Column(nullable = false)
    private BigDecimal hours;
}
