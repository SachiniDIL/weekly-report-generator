package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {}
