package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.TaskEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskEntryRepository extends JpaRepository<TaskEntry, Long> {}
