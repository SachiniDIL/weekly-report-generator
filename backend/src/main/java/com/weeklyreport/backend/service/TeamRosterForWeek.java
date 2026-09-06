package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.MemberWeekStatus;
import com.weeklyreport.backend.repository.ReportRepository;
import com.weeklyreport.backend.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * Every active member paired with their report status for a week (NOT_STARTED when they have no
 * report). The spine shared by the "submission status" chart and the section-comparison view, so
 * both stay consistent about who counts as a team member and how a status rolls up.
 */
@Component
class TeamRosterForWeek {

    private static final Sort BY_NAME = Sort.by("name").ascending();

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    TeamRosterForWeek(UserRepository userRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }

    List<MemberWeek> forWeek(LocalDate weekStart, LocalDate weekEnd) {
        Map<Long, MemberWeekStatus> statusByMemberId =
                reportRepository.findByWeekStartAndWeekEnd(weekStart, weekEnd).stream()
                        .collect(Collectors.toMap(
                                report -> report.getUser().getId(),
                                report -> MemberWeekStatus.of(report.getStatus()),
                                MemberWeekStatus::leastAdvanced));

        return userRepository.findByStatusAndRole(UserStatus.ACTIVE, Role.MEMBER, BY_NAME).stream()
                .map(member -> new MemberWeek(
                        member.getId(),
                        member.getName(),
                        statusByMemberId.getOrDefault(member.getId(), MemberWeekStatus.NOT_STARTED)))
                .toList();
    }

    record MemberWeek(long userId, String name, MemberWeekStatus status) {}
}
