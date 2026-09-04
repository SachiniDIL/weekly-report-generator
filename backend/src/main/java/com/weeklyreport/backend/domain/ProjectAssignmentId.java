package com.weeklyreport.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

/**
 * Composite key for {@link ProjectAssignment}. Records are Hibernate 6.2+'s recommended shape
 * for an {@code @EmbeddedId} — no hand-written equals/hashCode/getters needed.
 */
@Embeddable
public record ProjectAssignmentId(
        @Column(name = "user_id") Long userId, @Column(name = "project_id") Long projectId)
        implements Serializable {}
