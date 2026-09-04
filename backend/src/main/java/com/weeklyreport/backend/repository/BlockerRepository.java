package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Blocker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockerRepository extends JpaRepository<Blocker, Long> {

    List<Blocker> findByReportVersionId(Long reportVersionId);

    void deleteByReportVersionId(Long reportVersionId);
}
