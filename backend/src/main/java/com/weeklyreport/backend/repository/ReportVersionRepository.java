package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.ReportVersion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportVersionRepository extends JpaRepository<ReportVersion, Long> {

    Optional<ReportVersion> findByReportIdAndVersionNo(Long reportId, int versionNo);
}
