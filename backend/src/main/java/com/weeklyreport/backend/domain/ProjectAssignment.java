package com.weeklyreport.backend.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * A pure join between a user and a project — no columns beyond the composite key. Kept free of
 * {@code @ManyToOne} associations to {@link User}/{@link Project} since nothing yet needs to
 * navigate from an assignment back to those entities; add them when a real use case does.
 */
@Entity
@Table(name = "project_assignments")
@Getter
@Setter
public class ProjectAssignment {

    @EmbeddedId
    private ProjectAssignmentId id;
}
