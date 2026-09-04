package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByActiveTrue();
}
