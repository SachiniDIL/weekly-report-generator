package com.weeklyreport.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * One version of a {@link Report}'s content. Mutable — its content rows get replaced wholesale
 * on each edit — for as long as the parent report's status is DRAFT/NEEDS_CORRECTION; submitting
 * stamps {@code submittedAt} and the version is thereafter treated as fixed history.
 */
@Entity
@Table(name = "report_versions")
@Getter
@Setter
public class ReportVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The version-snapshot logic (a later step) needs to read and bump the parent report's
    // currentVersionNo when creating a new version, so this association is a genuine, immediate
    // need rather than a speculative one.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    // Defaults to its creation time (also covering the NOT NULL column while the version is
    // still a draft) and is overwritten with the real submission time by ReportService — hence
    // no updatable = false, unlike Report.createdAt.
    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "tasks_planned_next")
    private String tasksPlannedNext;

    private String notes;

    private String links;
}
