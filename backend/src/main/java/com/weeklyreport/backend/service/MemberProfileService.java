package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.dto.MemberProfileResponse;
import com.weeklyreport.backend.exception.UserNotFoundException;
import com.weeklyreport.backend.repository.DashboardChartRepository;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Assembles one team member's profile stats for a manager. Reuses the dashboard aggregation
 * queries, parameterized to a single member rather than team-wide.
 */
@Service
public class MemberProfileService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final DashboardChartRepository chartRepository;

    public MemberProfileService(
            UserRepository userRepository,
            ReportRepository reportRepository,
            DashboardChartRepository chartRepository) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.chartRepository = chartRepository;
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberProfile(long userId) {
        // A manager viewing another manager's or an admin's "profile" isn't a thing here.
        User member = userRepository
                .findById(userId)
                .filter(user -> user.getRole() == Role.MEMBER)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return new MemberProfileResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                reportRepository.countByUserIdAndStatusNot(userId, ReportStatus.DRAFT),
                reportRepository.countByUserIdAndStatus(userId, ReportStatus.NEEDS_CORRECTION),
                chartRepository.hoursByTaskType(userId, null, null));
    }
}
