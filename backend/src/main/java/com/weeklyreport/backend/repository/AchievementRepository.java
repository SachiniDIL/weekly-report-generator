package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Achievement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByReportVersionId(Long reportVersionId);

    void deleteByReportVersionId(Long reportVersionId);
}
