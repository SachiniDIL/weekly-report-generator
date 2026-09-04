package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.Blocker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockerRepository extends JpaRepository<Blocker, Long> {}
