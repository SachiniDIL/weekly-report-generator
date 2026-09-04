package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.ReportVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportVersionRepository extends JpaRepository<ReportVersion, Long> {}
