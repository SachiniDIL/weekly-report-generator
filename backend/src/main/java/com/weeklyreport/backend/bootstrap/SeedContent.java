package com.weeklyreport.backend.bootstrap;

import com.weeklyreport.backend.dto.ReportContentRequest;
import com.weeklyreport.backend.dto.ReportContentRequest.AchievementRequest;
import com.weeklyreport.backend.dto.ReportContentRequest.BlockerRequest;
import com.weeklyreport.backend.dto.ReportContentRequest.HoursBreakdownRequest;
import com.weeklyreport.backend.dto.ReportContentRequest.TaskEntryRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * Realistic report content for the seed dataset — one method per work theme. The prose is
 * deliberately concrete: the dashboard charts and the AI assistant both read this data and need
 * something meaningful to show and summarize.
 */
final class SeedContent {

    // The task types the "time by task type" chart groups on.
    private static final String DEV = "Development";
    private static final String TEST = "Testing";
    private static final String MEET = "Meetings";
    private static final String DOCS = "Documentation";

    private SeedContent() {}

    // ---- Client A ----

    /**
     * The one report that gets corrected twice. It stays free of key blockers/achievements
     * across all three versions on purpose: {@code ReportService} replaces a version's content
     * wholesale, and swapping one flagged row for another in a single flush would trip the
     * "one key per version" index. Its job here is to exercise version history, not flagging.
     */
    static ReportContentRequest checkoutRedesign() {
        return content(
                "Wire the promo-code field into the checkout state machine and start the payment step.",
                "Design review moved the card layout to a single column on mobile.",
                "https://github.com/acme/web/pull/2481",
                List.of(
                        task("Rebuild the checkout address step in React", "HIGH", 100, 100, "DONE", 16, 15,
                                "Address step live behind the checkout_v2 flag"),
                        task("Promo-code field", "MEDIUM", 80, 45, "IN_PROGRESS", 8, 9, null),
                        task("Add analytics events for each checkout step", "LOW", 50, 20, "IN_PROGRESS", 4, 3, null)),
                List.of(blocker(
                        "Payments team hasn't finalised the tokenization API, so the card step is stubbed", false)),
                List.of(
                        achievement("Shipped the checkout address step to a slice of traffic with no error-rate change", false),
                        achievement("Cut the checkout bundle by 40KB by dropping the old form library", false)),
                List.of(hrs(DEV, "14.0"), hrs(TEST, "6.5"), hrs(MEET, "3.0"), hrs(DOCS, "1.5")));
    }

    static ReportContentRequest checkoutRedesignRevisionOne() {
        return content(
                "Finish promo-code validation and pick up the payment step once tokenization lands.",
                "Split the promo-code work per review feedback.",
                "https://github.com/acme/web/pull/2481",
                List.of(
                        task("Rebuild the checkout address step in React", "HIGH", 100, 100, "DONE", 16, 15,
                                "Address step live behind the checkout_v2 flag"),
                        task("Promo-code input field and formatting", "MEDIUM", 100, 100, "DONE", 5, 5,
                                "Field merged in PR #2489"),
                        task("Promo-code validation and error states", "MEDIUM", 60, 40, "IN_PROGRESS", 5, 4, null),
                        task("Add analytics events for each checkout step", "LOW", 50, 30, "IN_PROGRESS", 4, 4, null)),
                List.of(blocker(
                        "Payments team hasn't finalised the tokenization API, so the card step is stubbed", false)),
                List.of(
                        achievement("Shipped the checkout address step to a slice of traffic with no error-rate change", false),
                        achievement("Cut the checkout bundle by 40KB by dropping the old form library", false)),
                List.of(hrs(DEV, "13.0"), hrs(TEST, "7.0"), hrs(MEET, "3.0"), hrs(DOCS, "1.0")));
    }

    static ReportContentRequest checkoutRedesignRevisionTwo() {
        return content(
                "Start the payment step now that the tokenization API is stable.",
                "Rollout percentage added so we can track adoption week to week.",
                "https://github.com/acme/web/pull/2481",
                List.of(
                        task("Promo-code validation and error states", "MEDIUM", 100, 100, "DONE", 5, 6,
                                "Validation live behind the checkout_v2 flag"),
                        task("Add analytics events for each checkout step", "LOW", 100, 100, "DONE", 4, 5,
                                "Events flowing to the analytics pipeline"),
                        task("Scaffold the card payment step", "HIGH", 40, 15, "IN_PROGRESS", 8, 3, null)),
                List.of(),
                List.of(
                        achievement("Checkout v2 now at 25% of traffic with conversion flat or better", false),
                        achievement("Promo codes fully migrated to the new form library", false)),
                List.of(hrs(DEV, "12.0"), hrs(TEST, "8.0"), hrs(MEET, "2.5"), hrs(DOCS, "1.5")));
    }

    static ReportContentRequest paymentProviderIntegration() {
        return content(
                "Run the full refund flow against the provider sandbox and document the webhook contract.",
                "Provider sandbox was down Tuesday, pushed webhook testing to Wednesday.",
                "https://github.com/acme/payments/pull/812",
                List.of(
                        task("Implement charge and capture against the provider SDK", "HIGH", 100, 100, "DONE", 20, 22,
                                "Charge/capture merged and covered by integration tests"),
                        task("Handle webhook signature verification", "HIGH", 100, 90, "IN_REVIEW", 8, 7, null),
                        task("Map provider error codes to our checkout error surface", "MEDIUM", 70, 60, "IN_PROGRESS", 6, 5, null)),
                List.of(blocker("Refund webhooks arrive out of order in the sandbox — waiting on provider support", false)),
                List.of(
                        achievement("End-to-end charge/capture working against the provider sandbox", true),
                        achievement("Added a replay-safe webhook handler with idempotency keys", false)),
                List.of(hrs(DEV, "16.0"), hrs(TEST, "9.0"), hrs(MEET, "2.0"), hrs(DOCS, "3.0")));
    }

    static ReportContentRequest customerDataExport() {
        return content(
                "Add CSV export alongside JSON and write the customer-facing help article.",
                "Legal signed off on the export field list.",
                "https://github.com/acme/web/pull/2506",
                List.of(
                        task("Build the async export job and download endpoint", "HIGH", 100, 85, "IN_REVIEW", 14, 13, null),
                        task("Redact PII fields the customer isn't entitled to", "HIGH", 100, 100, "DONE", 6, 8,
                                "Redaction rules unit-tested against the entitlement matrix"),
                        task("Draft the help-centre article for self-serve export", "LOW", 60, 60, "IN_PROGRESS", 3, 3, null)),
                List.of(),
                List.of(
                        achievement("Export job runs in under 30s for the largest test account", true),
                        achievement("PII redaction now shares one rule set with the API layer", false)),
                List.of(hrs(DEV, "11.0"), hrs(TEST, "5.0"), hrs(MEET, "2.5"), hrs(DOCS, "6.0")));
    }

    // ---- Internal Tooling ----

    static ReportContentRequest ciPipelineHardening() {
        return content(
                "Quarantine the remaining flaky suites and add a retry budget to the pipeline.",
                "Most flakes trace back to shared test database state.",
                "https://github.com/acme/infra/pull/331",
                List.of(
                        task("Parallelise the backend test job by package", "HIGH", 100, 100, "DONE", 10, 11,
                                "CI wall time down from 22 to 9 minutes"),
                        task("Track down intermittent failures in the reporting suites", "HIGH", 80, 50, "IN_PROGRESS", 8, 10, null),
                        task("Add a nightly job that runs tests 20x to surface flakes", "MEDIUM", 100, 100, "DONE", 4, 3,
                                "Nightly flake report posted to #eng-quality")),
                List.of(blocker("Some flaky suites still can't be reproduced locally — only fail under CI load", true)),
                List.of(
                        achievement("Cut CI wall time by 60% with per-package parallelism", true),
                        achievement("Flaky-test rate on main dropped from ~4% to ~1% of runs", false)),
                List.of(hrs(DEV, "9.0"), hrs(TEST, "12.0"), hrs(MEET, "2.0"), hrs(DOCS, "2.0")));
    }

    static ReportContentRequest adminDashboard() {
        return content(
                "Add the per-team drill-down and wire up CSV download for each table.",
                "Product wants the compliance number front and centre.",
                "https://github.com/acme/web/pull/2517",
                List.of(
                        task("Build the summary metric cards", "HIGH", 100, 100, "DONE", 9, 8,
                                "Cards live on the internal admin dashboard"),
                        task("Add the submission-status-by-member list", "MEDIUM", 100, 100, "DONE", 6, 6,
                                "List shipped with status chips"),
                        task("Per-team drill-down view", "MEDIUM", 40, 20, "IN_PROGRESS", 8, 4, null)),
                List.of(),
                List.of(
                        achievement("Managers can now see compliance and open-blocker counts at a glance", true),
                        achievement("Reused the reporting query layer instead of adding new endpoints", false)),
                List.of(hrs(DEV, "12.0"), hrs(TEST, "3.0"), hrs(MEET, "5.0"), hrs(DOCS, "1.0")));
    }

    static ReportContentRequest oncallRunbookAutomation() {
        return content(
                "Convert the two highest-traffic runbooks into scripts and dry-run them in staging.",
                "On-call feedback: the manual steps are where mistakes happen at 3am.",
                "https://github.com/acme/infra/pull/344",
                List.of(
                        task("Audit every runbook and tag the automatable steps", "MEDIUM", 100, 100, "DONE", 5, 6,
                                "Runbook audit spreadsheet shared with the team"),
                        task("Script the 'stuck queue' recovery runbook", "HIGH", 100, 70, "IN_REVIEW", 8, 9, null),
                        task("Rewrite the incident-comms template", "LOW", 100, 100, "DONE", 2, 2,
                                "New template in the incident repo")),
                List.of(blocker("Staging doesn't reproduce the queue backlog scenario, so the script is only partly tested", false)),
                List.of(
                        achievement("Half of the on-call runbook steps are now one command instead of a checklist", true),
                        achievement("Documented every runbook owner so stale ones get flagged", false)),
                List.of(hrs(DEV, "7.0"), hrs(TEST, "3.0"), hrs(MEET, "3.0"), hrs(DOCS, "9.0")));
    }

    // ---- R&D ----

    static ReportContentRequest vectorSearchPrototype() {
        return content(
                "Benchmark recall against the current keyword search on the support-ticket corpus.",
                "This is a two-week spike, not committed work yet.",
                "https://github.com/acme/research/pull/57",
                List.of(
                        task("Stand up a pgvector index over the ticket corpus", "HIGH", 100, 100, "DONE", 10, 12,
                                "Index and ingestion script in the research repo"),
                        task("Compare embedding models on retrieval quality", "MEDIUM", 80, 55, "IN_PROGRESS", 8, 7, null),
                        task("Write up cost and latency numbers for a go/no-go", "MEDIUM", 30, 10, "IN_PROGRESS", 4, 2, null)),
                List.of(blocker("Embedding API rate limits make full-corpus runs take hours — batching helps but not enough", false)),
                List.of(
                        achievement("Semantic search beats keyword recall by ~18% on the eval set", true),
                        achievement("Ingestion pipeline reusable for other corpora with a config change", false)),
                List.of(hrs(DEV, "13.0"), hrs(TEST, "4.0"), hrs(MEET, "4.0"), hrs(DOCS, "3.0")));
    }

    static ReportContentRequest llmEvalHarness() {
        return content(
                "Add regression tracking so we can see quality drift between model versions.",
                "Goal is a number we trust before we swap the production model.",
                "https://github.com/acme/research/pull/61",
                List.of(
                        task("Build a labelled eval set from real support transcripts", "HIGH", 100, 100, "DONE", 12, 13,
                                "300-example eval set with rubric"),
                        task("Implement the grading harness with pass/fail per rubric item", "HIGH", 100, 80, "IN_REVIEW", 10, 9, null),
                        task("Dashboard the results per model version", "LOW", 40, 25, "IN_PROGRESS", 5, 3, null)),
                List.of(),
                List.of(
                        achievement("Harness runs the full eval set in under five minutes", true),
                        achievement("Caught a 6-point quality regression in a candidate model before rollout", false)),
                List.of(hrs(DEV, "14.0"), hrs(TEST, "8.0"), hrs(MEET, "2.0"), hrs(DOCS, "2.0")));
    }

    static ReportContentRequest realtimeCollabSpike() {
        return content(
                "Prove multi-cursor editing survives reconnects, then write the trade-off memo.",
                "Spike to decide CRDT vs OT for the shared report editor.",
                "https://github.com/acme/research/pull/64",
                List.of(
                        task("Prototype a CRDT-backed shared text buffer", "HIGH", 100, 90, "IN_REVIEW", 16, 18, null),
                        task("Test presence and cursor sync across three clients", "MEDIUM", 70, 45, "IN_PROGRESS", 6, 5, null),
                        task("Draft the CRDT-vs-OT recommendation memo", "MEDIUM", 20, 5, "IN_PROGRESS", 4, 1, null)),
                List.of(blocker(
                        "The websocket gateway drops idle connections at 60s, breaking reconnect tests — needs an infra change", true)),
                List.of(
                        achievement("Two clients can edit the same document with automatic conflict resolution", true),
                        achievement("Measured sync latency under 80ms on a simulated poor connection", false)),
                List.of(hrs(DEV, "18.0"), hrs(TEST, "5.0"), hrs(MEET, "2.0"), hrs(DOCS, "1.0")));
    }

    // ---- builder helpers ----

    private static ReportContentRequest content(
            String tasksPlannedNext,
            String notes,
            String links,
            List<TaskEntryRequest> taskEntries,
            List<BlockerRequest> blockers,
            List<AchievementRequest> achievements,
            List<HoursBreakdownRequest> hoursBreakdown) {
        return new ReportContentRequest(
                tasksPlannedNext, notes, links, taskEntries, blockers, achievements, hoursBreakdown);
    }

    private static TaskEntryRequest task(
            String taskName,
            String priority,
            int plannedPct,
            int actualPct,
            String status,
            Integer timePlanned,
            Integer timeSpent,
            String deliverable) {
        return new TaskEntryRequest(
                taskName, priority, plannedPct, actualPct, status, timePlanned, timeSpent, deliverable);
    }

    private static BlockerRequest blocker(String description, boolean keyIssue) {
        return new BlockerRequest(description, keyIssue);
    }

    private static AchievementRequest achievement(String description, boolean keyHighlight) {
        return new AchievementRequest(description, keyHighlight);
    }

    private static HoursBreakdownRequest hrs(String taskType, String hours) {
        return new HoursBreakdownRequest(taskType, new BigDecimal(hours));
    }
}
