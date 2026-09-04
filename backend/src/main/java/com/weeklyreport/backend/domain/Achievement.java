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
 * An achievement reported within a {@link ReportVersion} snapshot. At most one per version may
 * have {@code keyHighlight} set — enforced by a partial unique index, not application code.
 */
@Entity
@Table(name = "achievements")
@Getter
@Setter
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_version_id", nullable = false)
    private Long reportVersionId;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_key_highlight", nullable = false)
    private boolean keyHighlight = false;
}
