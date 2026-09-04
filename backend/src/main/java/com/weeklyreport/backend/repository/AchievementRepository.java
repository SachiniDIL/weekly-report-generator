package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {}
