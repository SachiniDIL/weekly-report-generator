package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.HoursBreakdown;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoursBreakdownRepository extends JpaRepository<HoursBreakdown, Long> {

    List<HoursBreakdown> findByReportVersionId(Long reportVersionId);

    void deleteByReportVersionId(Long reportVersionId);
}
