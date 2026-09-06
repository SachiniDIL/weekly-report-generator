package com.weeklyreport.backend.service;

import com.weeklyreport.backend.domain.ReportStatus;
import com.weeklyreport.backend.dto.DashboardSection;
import com.weeklyreport.backend.dto.MemberSectionView;
import com.weeklyreport.backend.dto.MemberSectionView.SectionItem;
import com.weeklyreport.backend.repository.AchievementRepository;
import com.weeklyreport.backend.repository.BlockerRepository;
import com.weeklyreport.backend.repository.SectionItemProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Side-by-side view of one section (blockers or achievements) across the whole team for a week:
 * current version only, active members only, and members with nothing submitted still listed.
 */
@Service
public class SectionComparisonService {

    private final DashboardCalendar calendar;
    private final TeamRosterForWeek teamRoster;
    private final BlockerRepository blockerRepository;
    private final AchievementRepository achievementRepository;

    public SectionComparisonService(
            DashboardCalendar calendar,
            TeamRosterForWeek teamRoster,
            BlockerRepository blockerRepository,
            AchievementRepository achievementRepository) {
        this.calendar = calendar;
        this.teamRoster = teamRoster;
        this.blockerRepository = blockerRepository;
        this.achievementRepository = achievementRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberSectionView> compareSection(
            DashboardSection section, LocalDate weekStart, LocalDate weekEnd) {
        DashboardCalendar.WeekRange week = calendar.resolveWeek(weekStart, weekEnd);
        Map<Long, List<SectionItem>> itemsByMemberId = itemsByMemberId(section, week.start(), week.end());

        return teamRoster.forWeek(week.start(), week.end()).stream()
                .map(member -> new MemberSectionView(
                        member.name(),
                        member.status(),
                        itemsByMemberId.getOrDefault(member.userId(), List.of())))
                .toList();
    }

    private Map<Long, List<SectionItem>> itemsByMemberId(
            DashboardSection section, LocalDate weekStart, LocalDate weekEnd) {
        List<SectionItemProjection> rows =
                switch (section) {
                    case BLOCKERS -> blockerRepository.findCurrentVersionBlockersForWeek(
                            ReportStatus.DRAFT, weekStart, weekEnd);
                    case ACHIEVEMENTS -> achievementRepository.findCurrentVersionAchievementsForWeek(
                            ReportStatus.DRAFT, weekStart, weekEnd);
                };

        return rows.stream()
                .collect(Collectors.groupingBy(
                        SectionItemProjection::getUserId,
                        Collectors.mapping(
                                row -> new SectionItem(row.getDescription(), row.isKeyItem()),
                                Collectors.toList())));
    }
}
