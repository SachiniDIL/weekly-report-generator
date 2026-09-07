package com.weeklyreport.backend.bootstrap;

import com.weeklyreport.backend.dto.ReportContentRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The declarative shape of the seed dataset — who exists, which projects they work on, and the
 * week-by-week reports to build. Kept separate from {@link ProductionSeeder} so the seeder itself
 * stays a short orchestrator rather than a wall of data.
 */
final class SeedCatalog {

    /** One fixed password for every seeded account; logged on first run. */
    static final String PASSWORD = "Seeded@123";

    static final String CLIENT_A = "Client A";
    static final String INTERNAL_TOOLING = "Internal Tooling";
    static final String RND = "R&D";

    static final List<SeededProject> PROJECTS = List.of(
            new SeededProject(CLIENT_A, "External delivery work for our largest client — checkout, payments, data export."),
            new SeededProject(INTERNAL_TOOLING, "Engineering-productivity work: CI, internal dashboards, on-call tooling."),
            new SeededProject(RND, "Time-boxed research spikes feeding next quarter's roadmap."));

    static final List<SeededUser> MANAGERS = List.of(
            new SeededUser("Maya Okafor", "manager1@seed.dev"),
            new SeededUser("Daniel Reyes", "manager2@seed.dev"));

    static final List<SeededUser> MEMBERS = List.of(
            new SeededUser("Priya Sharma", "member1@seed.dev"),
            new SeededUser("Tom Becker", "member2@seed.dev"),
            new SeededUser("Aisha Bello", "member3@seed.dev"),
            new SeededUser("Leo Nakamura", "member4@seed.dev"),
            new SeededUser("Sofia Martin", "member5@seed.dev"));

    /** Member email → the projects they're assigned to (some are on more than one). */
    static final Map<String, List<String>> ASSIGNMENTS = Map.of(
            "member1@seed.dev", List.of(CLIENT_A, INTERNAL_TOOLING),
            "member2@seed.dev", List.of(CLIENT_A),
            "member3@seed.dev", List.of(INTERNAL_TOOLING, RND),
            "member4@seed.dev", List.of(RND),
            "member5@seed.dev", List.of(CLIENT_A, RND));

    private SeedCatalog() {}

    /** R&D reports are reviewed by manager2; everything else by manager1. */
    static String reviewerEmailFor(String projectName) {
        return RND.equals(projectName) ? "manager2@seed.dev" : "manager1@seed.dev";
    }

    record SeededUser(String name, String email) {}

    record SeededProject(String name, String description) {}

    /**
     * How far to drive one report through the lifecycle. {@code CORRECTED_AND_APPROVED} is the
     * one that exercises multi-version history: submit → changes → revise → submit → changes →
     * revise → submit → approve.
     */
    enum Lifecycle {
        DRAFT,
        SUBMITTED,
        NEEDS_CORRECTION,
        APPROVED,
        CORRECTED_AND_APPROVED
    }

    /** A manager's correction: the comment, plus the revised content the member submits next. */
    record CorrectionCycle(String managerComment, ReportContentRequest revisedContent) {}

    record SeededReport(
            String memberEmail,
            String projectName,
            int weeksAgo,
            Lifecycle lifecycle,
            ReportContentRequest content,
            List<CorrectionCycle> corrections) {

        static SeededReport of(
                String memberEmail, String projectName, int weeksAgo, Lifecycle lifecycle, ReportContentRequest content) {
            return new SeededReport(memberEmail, projectName, weeksAgo, lifecycle, content, List.of());
        }
    }

    static List<SeededReport> reports() {
        List<SeededReport> reports = new ArrayList<>();

        // member1 (Priya) — Client A + Internal Tooling, 4 weeks, including the twice-corrected report.
        reports.add(new SeededReport(
                "member1@seed.dev", CLIENT_A, 3, Lifecycle.CORRECTED_AND_APPROVED,
                SeedContent.checkoutRedesign(),
                List.of(
                        new CorrectionCycle(
                                "The \"Promo-code field\" task is too vague and has no deliverable — split it into the "
                                        + "field work and the validation work, and link what actually merged.",
                                SeedContent.checkoutRedesignRevisionOne()),
                        new CorrectionCycle(
                                "Better. Add the rollout percentage to the \"checkout v2\" achievement so we can track "
                                        + "adoption week to week.",
                                SeedContent.checkoutRedesignRevisionTwo()))));
        reports.add(SeededReport.of("member1@seed.dev", CLIENT_A, 2, Lifecycle.APPROVED, SeedContent.paymentProviderIntegration()));
        reports.add(new SeededReport(
                "member1@seed.dev", INTERNAL_TOOLING, 1, Lifecycle.NEEDS_CORRECTION,
                SeedContent.ciPipelineHardening(),
                List.of(new CorrectionCycle(
                        "Which reporting suites are still flaky? Name them so the next person can pick this up.", null))));
        reports.add(SeededReport.of("member1@seed.dev", CLIENT_A, 0, Lifecycle.SUBMITTED, SeedContent.customerDataExport()));

        // member2 (Tom) — Client A only, 3 weeks.
        reports.add(SeededReport.of("member2@seed.dev", CLIENT_A, 3, Lifecycle.APPROVED, SeedContent.paymentProviderIntegration()));
        reports.add(SeededReport.of("member2@seed.dev", CLIENT_A, 2, Lifecycle.APPROVED, SeedContent.customerDataExport()));
        reports.add(SeededReport.of("member2@seed.dev", CLIENT_A, 1, Lifecycle.SUBMITTED, SeedContent.checkoutRedesign()));

        // member3 (Aisha) — Internal Tooling + R&D, 4 weeks.
        reports.add(SeededReport.of("member3@seed.dev", INTERNAL_TOOLING, 3, Lifecycle.APPROVED, SeedContent.adminDashboard()));
        reports.add(new SeededReport(
                "member3@seed.dev", RND, 2, Lifecycle.NEEDS_CORRECTION,
                SeedContent.vectorSearchPrototype(),
                List.of(new CorrectionCycle(
                        "Add the actual cost and latency numbers — a go/no-go needs figures, not \"in progress\".", null))));
        reports.add(SeededReport.of("member3@seed.dev", INTERNAL_TOOLING, 1, Lifecycle.SUBMITTED, SeedContent.oncallRunbookAutomation()));
        reports.add(SeededReport.of("member3@seed.dev", RND, 0, Lifecycle.DRAFT, SeedContent.llmEvalHarness()));

        // member4 (Leo) — R&D only, 3 weeks.
        reports.add(SeededReport.of("member4@seed.dev", RND, 2, Lifecycle.APPROVED, SeedContent.llmEvalHarness()));
        reports.add(SeededReport.of("member4@seed.dev", RND, 1, Lifecycle.SUBMITTED, SeedContent.realtimeCollabSpike()));
        reports.add(SeededReport.of("member4@seed.dev", RND, 0, Lifecycle.DRAFT, SeedContent.vectorSearchPrototype()));

        // member5 (Sofia) — Client A + R&D, 3 weeks.
        reports.add(SeededReport.of("member5@seed.dev", CLIENT_A, 2, Lifecycle.APPROVED, SeedContent.customerDataExport()));
        reports.add(new SeededReport(
                "member5@seed.dev", RND, 1, Lifecycle.NEEDS_CORRECTION,
                SeedContent.realtimeCollabSpike(),
                List.of(new CorrectionCycle(
                        "The websocket gateway change you need — is there a ticket for infra yet? Link it.", null))));
        reports.add(SeededReport.of("member5@seed.dev", CLIENT_A, 0, Lifecycle.SUBMITTED, SeedContent.checkoutRedesign()));

        return reports;
    }
}
