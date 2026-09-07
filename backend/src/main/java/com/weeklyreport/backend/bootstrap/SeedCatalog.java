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
    static final String MOBILE_APP = "Mobile App";
    static final String PLATFORM_MIGRATION = "Platform Migration";

    static final List<SeededProject> PROJECTS = List.of(
            new SeededProject(CLIENT_A, "External delivery work for our largest client — checkout, payments, data export, search."),
            new SeededProject(INTERNAL_TOOLING, "Engineering-productivity work: CI, internal dashboards, on-call and developer tooling."),
            new SeededProject(RND, "Time-boxed research spikes feeding next quarter's roadmap."),
            new SeededProject(MOBILE_APP, "Native iOS/Android client: offline sync, push notifications, release automation."),
            new SeededProject(PLATFORM_MIGRATION, "Moving core services off the legacy monolith onto the new platform."));

    static final List<SeededUser> MANAGERS = List.of(
            new SeededUser("Maya Okafor", "manager1@seed.dev"),
            new SeededUser("Daniel Reyes", "manager2@seed.dev"),
            new SeededUser("Nadia Haddad", "manager3@seed.dev"));

    static final List<SeededUser> MEMBERS = List.of(
            new SeededUser("Priya Sharma", "member1@seed.dev"),
            new SeededUser("Tom Becker", "member2@seed.dev"),
            new SeededUser("Aisha Bello", "member3@seed.dev"),
            new SeededUser("Leo Nakamura", "member4@seed.dev"),
            new SeededUser("Sofia Martin", "member5@seed.dev"),
            new SeededUser("Kofi Mensah", "member6@seed.dev"),
            new SeededUser("Elena Petrova", "member7@seed.dev"),
            new SeededUser("Ravi Kapoor", "member8@seed.dev"),
            new SeededUser("Grace Lim", "member9@seed.dev"));

    /** Member email → the projects they're assigned to (most are on more than one). */
    static final Map<String, List<String>> ASSIGNMENTS = Map.ofEntries(
            Map.entry("member1@seed.dev", List.of(CLIENT_A, INTERNAL_TOOLING)),
            Map.entry("member2@seed.dev", List.of(CLIENT_A, MOBILE_APP)),
            Map.entry("member3@seed.dev", List.of(INTERNAL_TOOLING, RND)),
            Map.entry("member4@seed.dev", List.of(RND, PLATFORM_MIGRATION)),
            Map.entry("member5@seed.dev", List.of(CLIENT_A, RND)),
            Map.entry("member6@seed.dev", List.of(MOBILE_APP, CLIENT_A)),
            Map.entry("member7@seed.dev", List.of(PLATFORM_MIGRATION, INTERNAL_TOOLING)),
            Map.entry("member8@seed.dev", List.of(RND, PLATFORM_MIGRATION)),
            Map.entry("member9@seed.dev", List.of(MOBILE_APP, INTERNAL_TOOLING)));

    /** Which manager reviews each project's reports. */
    private static final Map<String, String> REVIEWER_BY_PROJECT = Map.of(
            CLIENT_A, "manager1@seed.dev",
            INTERNAL_TOOLING, "manager1@seed.dev",
            RND, "manager2@seed.dev",
            PLATFORM_MIGRATION, "manager2@seed.dev",
            MOBILE_APP, "manager3@seed.dev");

    private SeedCatalog() {}

    static String reviewerEmailFor(String projectName) {
        return REVIEWER_BY_PROJECT.getOrDefault(projectName, "manager1@seed.dev");
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

        // ── member1 (Priya) — Client A + Internal Tooling, 6 weeks, incl. the twice-corrected report ──
        reports.add(SeededReport.of("member1@seed.dev", CLIENT_A, 5, Lifecycle.APPROVED, SeedContent.paymentProviderIntegration()));
        reports.add(SeededReport.of("member1@seed.dev", CLIENT_A, 4, Lifecycle.APPROVED, SeedContent.customerDataExport()));
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
        reports.add(SeededReport.of("member1@seed.dev", CLIENT_A, 2, Lifecycle.APPROVED, SeedContent.searchRelevanceTuning()));
        reports.add(new SeededReport(
                "member1@seed.dev", INTERNAL_TOOLING, 1, Lifecycle.NEEDS_CORRECTION,
                SeedContent.ciPipelineHardening(),
                List.of(new CorrectionCycle(
                        "Which reporting suites are still flaky? Name them so the next person can pick this up.", null))));
        reports.add(SeededReport.of("member1@seed.dev", CLIENT_A, 0, Lifecycle.SUBMITTED, SeedContent.customerDataExport()));

        // ── member2 (Tom) — Client A + Mobile App, 5 weeks ──
        reports.add(SeededReport.of("member2@seed.dev", CLIENT_A, 4, Lifecycle.APPROVED, SeedContent.paymentProviderIntegration()));
        reports.add(SeededReport.of("member2@seed.dev", CLIENT_A, 3, Lifecycle.APPROVED, SeedContent.customerDataExport()));
        reports.add(SeededReport.of("member2@seed.dev", MOBILE_APP, 2, Lifecycle.APPROVED, SeedContent.pushNotificationsRework()));
        reports.add(new SeededReport(
                "member2@seed.dev", MOBILE_APP, 1, Lifecycle.NEEDS_CORRECTION,
                SeedContent.mobileReleaseAutomation(),
                List.of(new CorrectionCycle(
                        "Is the Play Console issue raised with Google, and what's the ticket number? Add it to the blocker.", null))));
        reports.add(SeededReport.of("member2@seed.dev", CLIENT_A, 0, Lifecycle.SUBMITTED, SeedContent.checkoutRedesign()));

        // ── member3 (Aisha) — Internal Tooling + R&D, 6 weeks ──
        reports.add(SeededReport.of("member3@seed.dev", INTERNAL_TOOLING, 5, Lifecycle.APPROVED, SeedContent.adminDashboard()));
        reports.add(SeededReport.of("member3@seed.dev", INTERNAL_TOOLING, 4, Lifecycle.APPROVED, SeedContent.featureFlagCleanup()));
        reports.add(SeededReport.of("member3@seed.dev", INTERNAL_TOOLING, 3, Lifecycle.APPROVED, SeedContent.developerPortalRefresh()));
        reports.add(new SeededReport(
                "member3@seed.dev", RND, 2, Lifecycle.NEEDS_CORRECTION,
                SeedContent.vectorSearchPrototype(),
                List.of(new CorrectionCycle(
                        "Add the actual cost and latency numbers — a go/no-go needs figures, not \"in progress\".", null))));
        reports.add(SeededReport.of("member3@seed.dev", INTERNAL_TOOLING, 1, Lifecycle.SUBMITTED, SeedContent.oncallRunbookAutomation()));
        reports.add(SeededReport.of("member3@seed.dev", RND, 0, Lifecycle.DRAFT, SeedContent.llmEvalHarness()));

        // ── member4 (Leo) — R&D + Platform Migration, 5 weeks ──
        reports.add(SeededReport.of("member4@seed.dev", RND, 4, Lifecycle.APPROVED, SeedContent.llmEvalHarness()));
        reports.add(SeededReport.of("member4@seed.dev", RND, 3, Lifecycle.APPROVED, SeedContent.agentToolingSpike()));
        reports.add(SeededReport.of("member4@seed.dev", PLATFORM_MIGRATION, 2, Lifecycle.APPROVED, SeedContent.legacyEndpointDeprecation()));
        reports.add(SeededReport.of("member4@seed.dev", PLATFORM_MIGRATION, 1, Lifecycle.SUBMITTED, SeedContent.dataStoreCutover()));
        reports.add(SeededReport.of("member4@seed.dev", RND, 0, Lifecycle.DRAFT, SeedContent.vectorSearchPrototype()));

        // ── member5 (Sofia) — Client A + R&D, 5 weeks ──
        reports.add(SeededReport.of("member5@seed.dev", CLIENT_A, 4, Lifecycle.APPROVED, SeedContent.customerDataExport()));
        reports.add(SeededReport.of("member5@seed.dev", CLIENT_A, 3, Lifecycle.APPROVED, SeedContent.searchRelevanceTuning()));
        reports.add(new SeededReport(
                "member5@seed.dev", RND, 2, Lifecycle.NEEDS_CORRECTION,
                SeedContent.realtimeCollabSpike(),
                List.of(new CorrectionCycle(
                        "The websocket gateway change you need — is there a ticket for infra yet? Link it.", null))));
        reports.add(SeededReport.of("member5@seed.dev", RND, 1, Lifecycle.APPROVED, SeedContent.agentToolingSpike()));
        reports.add(SeededReport.of("member5@seed.dev", CLIENT_A, 0, Lifecycle.SUBMITTED, SeedContent.checkoutRedesign()));

        // ── member6 (Kofi) — Mobile App + Client A, 6 weeks, incl. the offline-sync twice-corrected report ──
        reports.add(SeededReport.of("member6@seed.dev", MOBILE_APP, 5, Lifecycle.APPROVED, SeedContent.pushNotificationsRework()));
        reports.add(new SeededReport(
                "member6@seed.dev", MOBILE_APP, 4, Lifecycle.CORRECTED_AND_APPROVED,
                SeedContent.offlineSyncEngine(),
                List.of(
                        new CorrectionCycle(
                                "\"Conflict resolution\" is a whole project by itself. Scope it to last-write-wins for now, "
                                        + "call out the banner work separately, and link what merged.",
                                SeedContent.offlineSyncEngineRevisionOne()),
                        new CorrectionCycle(
                                "Good. Before I approve — what's the crash rate on the beta build with this on? "
                                        + "Add it to the achievement.",
                                SeedContent.offlineSyncEngineRevisionTwo()))));
        reports.add(SeededReport.of("member6@seed.dev", MOBILE_APP, 3, Lifecycle.APPROVED, SeedContent.mobileReleaseAutomation()));
        reports.add(SeededReport.of("member6@seed.dev", CLIENT_A, 2, Lifecycle.APPROVED, SeedContent.paymentProviderIntegration()));
        reports.add(SeededReport.of("member6@seed.dev", MOBILE_APP, 1, Lifecycle.SUBMITTED, SeedContent.pushNotificationsRework()));
        reports.add(SeededReport.of("member6@seed.dev", MOBILE_APP, 0, Lifecycle.DRAFT, SeedContent.mobileReleaseAutomation()));

        // ── member7 (Elena) — Platform Migration + Internal Tooling, 6 weeks, incl. the auth-migration twice-corrected report ──
        reports.add(SeededReport.of("member7@seed.dev", PLATFORM_MIGRATION, 5, Lifecycle.APPROVED, SeedContent.legacyEndpointDeprecation()));
        reports.add(new SeededReport(
                "member7@seed.dev", PLATFORM_MIGRATION, 4, Lifecycle.CORRECTED_AND_APPROVED,
                SeedContent.authServiceMigration(),
                List.of(
                        new CorrectionCycle(
                                "We are not doing a hard cutover on error shapes — clients will break. Build a compatibility "
                                        + "adapter first and notify the client teams.",
                                SeedContent.authServiceMigrationRevisionOne()),
                        new CorrectionCycle(
                                "Ready to approve once you add how long read traffic has been clean and who's still on the "
                                        + "old error shape.",
                                SeedContent.authServiceMigrationRevisionTwo()))));
        reports.add(SeededReport.of("member7@seed.dev", PLATFORM_MIGRATION, 3, Lifecycle.APPROVED, SeedContent.dataStoreCutover()));
        reports.add(SeededReport.of("member7@seed.dev", INTERNAL_TOOLING, 2, Lifecycle.APPROVED, SeedContent.featureFlagCleanup()));
        reports.add(new SeededReport(
                "member7@seed.dev", PLATFORM_MIGRATION, 1, Lifecycle.NEEDS_CORRECTION,
                SeedContent.dataStoreCutover(),
                List.of(new CorrectionCycle(
                        "The nightly 200-row diff — is it a data bug or a clock-skew artefact? I need to know before read cutover.", null))));
        reports.add(SeededReport.of("member7@seed.dev", INTERNAL_TOOLING, 0, Lifecycle.SUBMITTED, SeedContent.developerPortalRefresh()));

        // ── member8 (Ravi) — R&D + Platform Migration, 5 weeks ──
        reports.add(SeededReport.of("member8@seed.dev", RND, 4, Lifecycle.APPROVED, SeedContent.vectorSearchPrototype()));
        reports.add(SeededReport.of("member8@seed.dev", RND, 3, Lifecycle.APPROVED, SeedContent.agentToolingSpike()));
        reports.add(SeededReport.of("member8@seed.dev", PLATFORM_MIGRATION, 2, Lifecycle.APPROVED, SeedContent.authServiceMigration()));
        reports.add(SeededReport.of("member8@seed.dev", PLATFORM_MIGRATION, 1, Lifecycle.SUBMITTED, SeedContent.legacyEndpointDeprecation()));
        reports.add(SeededReport.of("member8@seed.dev", RND, 0, Lifecycle.SUBMITTED, SeedContent.llmEvalHarness()));

        // ── member9 (Grace) — Mobile App + Internal Tooling, 4 weeks (joined recently) ──
        reports.add(SeededReport.of("member9@seed.dev", INTERNAL_TOOLING, 3, Lifecycle.APPROVED, SeedContent.developerPortalRefresh()));
        reports.add(new SeededReport(
                "member9@seed.dev", MOBILE_APP, 2, Lifecycle.NEEDS_CORRECTION,
                SeedContent.offlineSyncEngine(),
                List.of(new CorrectionCycle(
                        "Attachments are out of scope for this iteration — drop that task and the blocker, and focus the "
                                + "report on what shipped.", null))));
        reports.add(SeededReport.of("member9@seed.dev", MOBILE_APP, 1, Lifecycle.SUBMITTED, SeedContent.pushNotificationsRework()));
        reports.add(SeededReport.of("member9@seed.dev", INTERNAL_TOOLING, 0, Lifecycle.DRAFT, SeedContent.featureFlagCleanup()));

        return reports;
    }
}
