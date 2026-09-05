package com.weeklyreport.backend.repository;

import com.weeklyreport.backend.domain.ReviewComment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    Optional<ReviewComment> findByReportVersionId(Long reportVersionId);
}
