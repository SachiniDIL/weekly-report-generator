package com.weeklyreport.backend.bootstrap;

import com.weeklyreport.backend.bootstrap.SeedCatalog.CorrectionCycle;
import com.weeklyreport.backend.bootstrap.SeedCatalog.SeededReport;
import com.weeklyreport.backend.bootstrap.SeedCatalog.SeededUser;
import com.weeklyreport.backend.domain.Project;
import com.weeklyreport.backend.domain.ProjectAssignment;
import com.weeklyreport.backend.domain.ProjectAssignmentId;
import com.weeklyreport.backend.domain.ReviewAction;
import com.weeklyreport.backend.domain.Role;
import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.domain.UserStatus;
import com.weeklyreport.backend.dto.CreateReportRequest;
import com.weeklyreport.backend.dto.ReviewRequest;
import com.weeklyreport.backend.repository.ProjectAssignmentRepository;
import com.weeklyreport.backend.repository.ProjectRepository;
import com.weeklyreport.backend.repository.UserRepository;
import com.weeklyreport.backend.service.ReportReviewService;
import com.weeklyreport.backend.service.ReportService;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the multi-member, multi-week demo dataset the assignment brief calls for, so the
 * dashboard and AI assistant have something real to show.
 *
 * <p>Guarded by the {@code seed} profile — activate it alongside {@code local} for local use, or
 * add it once to the deployed instance's profile list for the first boot and remove it after.
 * The run is idempotent: if the seed projects already exist it does nothing.
 *
 * <p>Reports are built through {@link ReportService} and {@link ReportReviewService} rather than
 * inserted directly, so version history, review comments and status transitions all match what
 * the real endpoints produce.
 */
@Component
@Profile("seed")
public class ProductionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductionSeeder.class);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportService reportService;
    private final ReportReviewService reportReviewService;
    private final Clock clock;

    public ProductionSeeder(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ProjectAssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder,
            ReportService reportService,
            ReportReviewService reportReviewService,
            Clock clock) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportService = reportService;
        this.reportReviewService = reportReviewService;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (projectRepository.existsByName(SeedCatalog.CLIENT_A)) {
            log.info("Seed data already present — ProductionSeeder is skipping.");
            return;
        }

        Map<String, User> managers = createUsers(SeedCatalog.MANAGERS, Role.MANAGER);
        Map<String, User> members = createUsers(SeedCatalog.MEMBERS, Role.MEMBER);
        Map<String, Project> projects = createProjects();
        assignMembersToProjects(members, projects);
        seedReports(members, managers, projects);

        logSeededAccounts();
    }

    private Map<String, User> createUsers(List<SeededUser> specs, Role role) {
        Map<String, User> byEmail = new LinkedHashMap<>();
        for (SeededUser spec : specs) {
            User user = new User();
            user.setName(spec.name());
            user.setEmail(spec.email());
            user.setPasswordHash(passwordEncoder.encode(SeedCatalog.PASSWORD));
            user.setStatus(UserStatus.ACTIVE);
            user.setRole(role);
            byEmail.put(spec.email(), userRepository.save(user));
        }
        return byEmail;
    }

    private Map<String, Project> createProjects() {
        Map<String, Project> byName = new LinkedHashMap<>();
        for (SeedCatalog.SeededProject spec : SeedCatalog.PROJECTS) {
            Project project = new Project();
            project.setName(spec.name());
            project.setDescription(spec.description());
            byName.put(spec.name(), projectRepository.save(project));
        }
        return byName;
    }

    private void assignMembersToProjects(Map<String, User> members, Map<String, Project> projects) {
        List<ProjectAssignment> assignments = new ArrayList<>();
        SeedCatalog.ASSIGNMENTS.forEach((memberEmail, projectNames) -> {
            long userId = members.get(memberEmail).getId();
            for (String projectName : projectNames) {
                ProjectAssignment assignment = new ProjectAssignment();
                assignment.setId(new ProjectAssignmentId(userId, projects.get(projectName).getId()));
                assignments.add(assignment);
            }
        });
        assignmentRepository.saveAll(assignments);
    }

    private void seedReports(
            Map<String, User> members, Map<String, User> managers, Map<String, Project> projects) {
        LocalDate currentWeekMonday = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (SeededReport spec : SeedCatalog.reports()) {
            User member = members.get(spec.memberEmail());
            User reviewer = managers.get(SeedCatalog.reviewerEmailFor(spec.projectName()));
            Project project = projects.get(spec.projectName());
            LocalDate weekStart = currentWeekMonday.minusWeeks(spec.weeksAgo());
            buildReport(spec, member, reviewer, project, weekStart);
        }
    }

    private void buildReport(SeededReport spec, User member, User reviewer, Project project, LocalDate weekStart) {
        CreateReportRequest createRequest =
                new CreateReportRequest(project.getId(), weekStart, weekStart.plusDays(6), null);
        long reportId = reportService.createReport(member, createRequest, spec.content()).id();

        switch (spec.lifecycle()) {
            case DRAFT -> {
                // Left as a draft.
            }
            case SUBMITTED -> reportService.submitReport(reportId, member);
            case APPROVED -> {
                reportService.submitReport(reportId, member);
                approve(reviewer, reportId);
            }
            case NEEDS_CORRECTION -> {
                reportService.submitReport(reportId, member);
                requestChanges(reviewer, reportId, spec.corrections().get(0).managerComment());
            }
            case CORRECTED_AND_APPROVED -> {
                for (CorrectionCycle cycle : spec.corrections()) {
                    reportService.submitReport(reportId, member);
                    requestChanges(reviewer, reportId, cycle.managerComment());
                    reportService.updateReportContent(reportId, member, cycle.revisedContent());
                }
                reportService.submitReport(reportId, member);
                approve(reviewer, reportId);
            }
        }
    }

    private void approve(User reviewer, long reportId) {
        reportReviewService.review(reviewer, reportId, new ReviewRequest(ReviewAction.APPROVED, null));
    }

    private void requestChanges(User reviewer, long reportId, String comment) {
        reportReviewService.review(
                reviewer, reportId, new ReviewRequest(ReviewAction.CHANGES_REQUESTED, comment));
    }

    private void logSeededAccounts() {
        log.info(
                "Seeded demo dataset: {} managers, {} members, {} projects, {} reports.",
                SeedCatalog.MANAGERS.size(),
                SeedCatalog.MEMBERS.size(),
                SeedCatalog.PROJECTS.size(),
                SeedCatalog.reports().size());
        log.info("  Managers: {}", emails(SeedCatalog.MANAGERS));
        log.info("  Members:  {}", emails(SeedCatalog.MEMBERS));
        log.info("  Password for every seeded account: {}", SeedCatalog.PASSWORD);
    }

    private static String emails(List<SeededUser> users) {
        return users.stream().map(SeededUser::email).collect(Collectors.joining(", "));
    }
}
