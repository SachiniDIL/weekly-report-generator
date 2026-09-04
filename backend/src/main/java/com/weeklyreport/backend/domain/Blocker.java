package com.weeklyreport.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A blocker reported within a {@link ReportVersion} snapshot. At most one per version may have
 * {@code keyIssue} set — enforced by a partial unique index, not application code.
 */
@Entity
@Table(name = "blockers")
@Getter
@Setter
public class Blocker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_version_id", nullable = false)
    private Long reportVersionId;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_key_issue", nullable = false)
    private boolean keyIssue = false;
}
