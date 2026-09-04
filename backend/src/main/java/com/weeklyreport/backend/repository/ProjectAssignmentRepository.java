package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.ProjectAssignment;
import com.weeklyreport.backend.domain.ProjectAssignmentId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAssignmentRepository
        extends JpaRepository<ProjectAssignment, ProjectAssignmentId> {

    List<ProjectAssignment> findById_ProjectId(Long projectId);
}
