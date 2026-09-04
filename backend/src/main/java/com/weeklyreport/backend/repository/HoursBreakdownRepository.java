package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.HoursBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoursBreakdownRepository extends JpaRepository<HoursBreakdown, Long> {}
