package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.TaskEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskEntryRepository extends JpaRepository<TaskEntry, Long> {

    List<TaskEntry> findByReportVersionId(Long reportVersionId);

    void deleteByReportVersionId(Long reportVersionId);
}
