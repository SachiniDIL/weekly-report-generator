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

    static ReportContentRequest searchRelevanceTuning() {
        return content(
                "Ship the synonym list and start on typo tolerance for the product search.",
                "Support tickets about \"can't find X\" dropped noticeably after the boosting change.",
                "https://github.com/acme/web/pull/2531",
                List.of(
                        task("Add field boosting so titles outrank descriptions", "HIGH", 100, 100, "DONE", 8, 7,
                                "Boosting live; click-through on the top result up 12%"),
                        task("Curate a synonym list from the top 200 zero-result queries", "MEDIUM", 100, 90, "IN_REVIEW", 6, 6, null),
                        task("Prototype typo tolerance with a bounded edit distance", "MEDIUM", 40, 20, "IN_PROGRESS", 5, 3, null)),
                List.of(),
                List.of(
                        achievement("Zero-result searches down from 9% to 5% of all queries", true),
                        achievement("Built a weekly report of the worst-performing queries for the PM", false)),
                List.of(hrs(DEV, "12.0"), hrs(TEST, "4.5"), hrs(MEET, "2.0"), hrs(DOCS, "1.5")));
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

    static ReportContentRequest featureFlagCleanup() {
        return content(
                "Delete the flags cleared for removal and add a staleness check to CI.",
                "We had 140 flags; roughly half outlived their rollout by months.",
                "https://github.com/acme/infra/pull/359",
                List.of(
                        task("Inventory every flag with its age, owner, and last evaluation", "HIGH", 100, 100, "DONE", 6, 5,
                                "Flag inventory dashboard shared in #eng"),
                        task("Remove the 30 flags fully rolled out for 90+ days", "HIGH", 100, 70, "IN_PROGRESS", 10, 8, null),
                        task("Add a CI warning when a flag has no evaluations in 60 days", "MEDIUM", 100, 100, "DONE", 4, 4,
                                "Check runs on every PR touching flag config")),
                List.of(),
                List.of(
                        achievement("Flag count down from 140 to 96 with no incidents", true),
                        achievement("New flags now require an expiry date at creation time", false)),
                List.of(hrs(DEV, "10.0"), hrs(TEST, "4.0"), hrs(MEET, "3.0"), hrs(DOCS, "2.0")));
    }

    static ReportContentRequest developerPortalRefresh() {
        return content(
                "Migrate the last three service docs pages and turn off the old wiki.",
                "Engineers kept landing on stale wiki pages from search, so this is mostly a redirect problem.",
                "https://github.com/acme/infra/pull/366",
                List.of(
                        task("Generate service pages from each repo's metadata file", "HIGH", 100, 100, "DONE", 9, 10,
                                "42 services now have an auto-generated page"),
                        task("Set up redirects from every old wiki URL", "MEDIUM", 100, 80, "IN_REVIEW", 5, 6, null),
                        task("Add an ownership widget pulling from the on-call schedule", "LOW", 60, 40, "IN_PROGRESS", 4, 3, null)),
                List.of(blocker("The old wiki's export doesn't preserve internal links, so redirects are being mapped by hand", false)),
                List.of(
                        achievement("Time to find a service owner dropped from minutes to one click", false),
                        achievement("Docs now regenerate on every merge instead of going stale", false)),
                List.of(hrs(DEV, "8.0"), hrs(TEST, "2.0"), hrs(MEET, "4.0"), hrs(DOCS, "8.0")));
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

    static ReportContentRequest agentToolingSpike() {
        return content(
                "Wire the retrieval tool into the agent loop and measure task completion on the eval set.",
                "Spike to see whether tool use closes the gap with a fine-tuned model.",
                "https://github.com/acme/research/pull/70",
                List.of(
                        task("Define a tool schema for search, fetch, and summarise", "HIGH", 100, 100, "DONE", 8, 7,
                                "Schema and mock tools in the research repo"),
                        task("Run the agent loop over the 300-example eval set", "HIGH", 80, 60, "IN_PROGRESS", 10, 9, null),
                        task("Log every tool call so we can see where the agent goes wrong", "MEDIUM", 100, 100, "DONE", 4, 5,
                                "Trace viewer renders each run step by step")),
                List.of(blocker("Long tool chains blow the context window on ~15% of tasks — needs a summarisation step", true)),
                List.of(
                        achievement("Tool use lifts task completion from 61% to 78% on the eval set", true),
                        achievement("Trace viewer already reused by two other research threads", false)),
                List.of(hrs(DEV, "15.0"), hrs(TEST, "6.0"), hrs(MEET, "3.0"), hrs(DOCS, "2.0")));
    }

    // ---- Mobile App ----

    /**
     * The second report that gets corrected twice — offline sync on the mobile client. Exercises
     * multi-version history on a different project and member than {@link #checkoutRedesign()}.
     */
    static ReportContentRequest offlineSyncEngine() {
        return content(
                "Get conflict resolution working for the note editor, then handle attachments.",
                "Field reports: reps lose edits on the subway every day. This is the top complaint.",
                "https://github.com/acme/mobile/pull/1204",
                List.of(
                        task("Queue writes locally and replay them on reconnect", "HIGH", 100, 90, "IN_REVIEW", 18, 20, null),
                        task("Conflict resolution", "HIGH", 60, 30, "IN_PROGRESS", 10, 6, null),
                        task("Show a sync status indicator in the app bar", "LOW", 100, 100, "DONE", 3, 3,
                                "Indicator ships in the next TestFlight build")),
                List.of(blocker("Attachment sync needs the new blob storage API, which infra hasn't scheduled", false)),
                List.of(
                        achievement("Text edits now survive a full offline session and replay cleanly", false),
                        achievement("Local write queue is storage-backed, so it survives an app kill", false)),
                List.of(hrs(DEV, "16.0"), hrs(TEST, "7.0"), hrs(MEET, "2.5"), hrs(DOCS, "1.0")));
    }

    static ReportContentRequest offlineSyncEngineRevisionOne() {
        return content(
                "Finish last-write-wins with a visible conflict banner, then attachments once blob storage lands.",
                "Split the conflict work per review feedback and linked the merged pieces.",
                "https://github.com/acme/mobile/pull/1204",
                List.of(
                        task("Queue writes locally and replay them on reconnect", "HIGH", 100, 100, "DONE", 18, 20,
                                "Replay merged in PR #1207"),
                        task("Last-write-wins resolution with a server timestamp", "HIGH", 100, 100, "DONE", 6, 7,
                                "Resolution logic merged and unit-tested"),
                        task("Conflict banner so the user knows a remote edit won", "MEDIUM", 70, 45, "IN_PROGRESS", 4, 3, null),
                        task("Show a sync status indicator in the app bar", "LOW", 100, 100, "DONE", 3, 3,
                                "Indicator shipped in TestFlight build 88")),
                List.of(blocker("Attachment sync needs the new blob storage API, which infra hasn't scheduled", false)),
                List.of(
                        achievement("Text edits now survive a full offline session and replay cleanly", false),
                        achievement("Local write queue is storage-backed, so it survives an app kill", false)),
                List.of(hrs(DEV, "15.0"), hrs(TEST, "8.0"), hrs(MEET, "2.5"), hrs(DOCS, "1.5")));
    }

    static ReportContentRequest offlineSyncEngineRevisionTwo() {
        return content(
                "Pick up attachment sync now that blob storage is in staging.",
                "Added the crash-rate number the review asked for.",
                "https://github.com/acme/mobile/pull/1204",
                List.of(
                        task("Conflict banner so the user knows a remote edit won", "MEDIUM", 100, 100, "DONE", 4, 5,
                                "Banner live in TestFlight build 91"),
                        task("Attachment upload queue on top of the blob storage API", "HIGH", 30, 10, "IN_PROGRESS", 10, 3, null)),
                List.of(),
                List.of(
                        achievement("Offline note editing shipped to the full beta group; sync-related crashes at zero over 9 days", false),
                        achievement("Replay logic reused as-is for the offline expense form", false)),
                List.of(hrs(DEV, "14.0"), hrs(TEST, "9.0"), hrs(MEET, "2.0"), hrs(DOCS, "1.0")));
    }

    static ReportContentRequest pushNotificationsRework() {
        return content(
                "Move the last two notification types onto the new template system and delete the old sender.",
                "Open rates are low because half our pushes have no deep link.",
                "https://github.com/acme/mobile/pull/1221",
                List.of(
                        task("Add deep links to every notification payload", "HIGH", 100, 100, "DONE", 8, 9,
                                "Every push now opens the relevant screen"),
                        task("Per-type notification preferences in settings", "MEDIUM", 100, 85, "IN_REVIEW", 7, 7, null),
                        task("Delete the legacy notification sender", "LOW", 40, 20, "IN_PROGRESS", 3, 2, null)),
                List.of(),
                List.of(
                        achievement("Push open rate up from 8% to 14% after adding deep links", true),
                        achievement("Notification code is now one path instead of three", false)),
                List.of(hrs(DEV, "11.0"), hrs(TEST, "5.0"), hrs(MEET, "3.0"), hrs(DOCS, "2.0")));
    }

    static ReportContentRequest mobileReleaseAutomation() {
        return content(
                "Get the release train to cut, build, and submit both stores with no manual steps.",
                "Cutting a release is a half-day of one engineer's time every two weeks.",
                "https://github.com/acme/mobile/pull/1233",
                List.of(
                        task("Automate version bump, changelog, and tag on a schedule", "HIGH", 100, 100, "DONE", 8, 8,
                                "Release branch cuts itself every other Monday"),
                        task("Upload builds to TestFlight and Play internal track from CI", "HIGH", 100, 90, "IN_REVIEW", 10, 11, null),
                        task("Post the release checklist to Slack with links", "LOW", 100, 100, "DONE", 2, 2,
                                "Checklist auto-posts to #mobile-releases")),
                List.of(blocker("Play Console API keeps rejecting the CI service account — waiting on a Google support ticket", true)),
                List.of(
                        achievement("iOS side of the release is fully hands-off through TestFlight", true),
                        achievement("Release notes now assembled from PR titles automatically", false)),
                List.of(hrs(DEV, "13.0"), hrs(TEST, "4.0"), hrs(MEET, "2.0"), hrs(DOCS, "3.0")));
    }

    // ---- Platform Migration ----

    static ReportContentRequest authServiceMigration() {
        return content(
                "Cut read traffic over to the new auth service and watch error rates for a week.",
                "This is the riskiest slice of the migration — every request touches auth.",
                "https://github.com/acme/platform/pull/489",
                List.of(
                        task("Shadow-run the new auth service against production traffic", "HIGH", 100, 100, "DONE", 14, 16,
                                "Two weeks of shadow traffic, response parity at 99.98%"),
                        task("Move token validation to the new service behind a flag", "HIGH", 100, 75, "IN_REVIEW", 10, 9, null),
                        task("Write the rollback runbook and rehearse it in staging", "MEDIUM", 100, 100, "DONE", 5, 6,
                                "Rollback rehearsed; takes under 3 minutes")),
                List.of(blocker("The legacy service and the new one disagree on how expired-token errors are shaped — clients depend on the old shape", true)),
                List.of(
                        achievement("New auth service handling 100% of shadow traffic with parity", true),
                        achievement("p99 token validation latency down 40% versus the monolith", false)),
                List.of(hrs(DEV, "15.0"), hrs(TEST, "10.0"), hrs(MEET, "4.0"), hrs(DOCS, "5.0")));
    }

    static ReportContentRequest authServiceMigrationRevisionOne() {
        return content(
                "Ship the error-shape adapter, then move token validation for real.",
                "Added the compatibility layer the review asked for instead of a hard cutover.",
                "https://github.com/acme/platform/pull/489",
                List.of(
                        task("Adapter that rewrites new-service errors into the legacy shape", "HIGH", 100, 100, "DONE", 8, 9,
                                "Adapter merged; contract tests cover every documented error code"),
                        task("Move token validation to the new service behind a flag", "HIGH", 100, 90, "IN_REVIEW", 10, 10, null),
                        task("Notify the three client teams still parsing raw error bodies", "MEDIUM", 100, 100, "DONE", 3, 2,
                                "All three ack'd; two have migrated already")),
                List.of(),
                List.of(
                        achievement("New auth service handling 100% of shadow traffic with parity", true),
                        achievement("p99 token validation latency down 40% versus the monolith", false)),
                List.of(hrs(DEV, "16.0"), hrs(TEST, "9.0"), hrs(MEET, "3.0"), hrs(DOCS, "4.0")));
    }

    static ReportContentRequest authServiceMigrationRevisionTwo() {
        return content(
                "Move write traffic once read traffic has been clean for a full week.",
                "Added the adoption timeline the review wanted.",
                "https://github.com/acme/platform/pull/489",
                List.of(
                        task("Move token validation to the new service behind a flag", "HIGH", 100, 100, "DONE", 10, 11,
                                "100% of read traffic on the new service for 8 days, error rate flat"),
                        task("Dashboard old-vs-new traffic split for the cutover", "MEDIUM", 100, 100, "DONE", 4, 4,
                                "Split visible in Grafana with an alert on divergence")),
                List.of(),
                List.of(
                        achievement("All auth read traffic now served by the new service; monolith auth path is dead code", true),
                        achievement("Cutover plan adopted as the template for the next two service migrations", false)),
                List.of(hrs(DEV, "12.0"), hrs(TEST, "8.0"), hrs(MEET, "5.0"), hrs(DOCS, "4.0")));
    }

    static ReportContentRequest dataStoreCutover() {
        return content(
                "Backfill the last table and run the dual-write reconciliation job to completion.",
                "Dual-write has been running for three weeks; now it's about proving the data matches.",
                "https://github.com/acme/platform/pull/502",
                List.of(
                        task("Backfill the orders table into the new store", "HIGH", 100, 100, "DONE", 12, 14,
                                "18M rows backfilled; row counts match to the row"),
                        task("Reconciliation job that diffs old vs new nightly", "HIGH", 100, 80, "IN_REVIEW", 8, 8, null),
                        task("Move read queries for the orders API to the new store", "MEDIUM", 30, 15, "IN_PROGRESS", 6, 3, null)),
                List.of(blocker("Reconciliation finds ~200 rows a night that differ on a timestamp field — tracking down the write path", false)),
                List.of(
                        achievement("Orders history fully backfilled and byte-identical on spot checks", false),
                        achievement("Dual-write lag holding under 500ms at peak", false)),
                List.of(hrs(DEV, "14.0"), hrs(TEST, "8.0"), hrs(MEET, "3.0"), hrs(DOCS, "3.0")));
    }

    static ReportContentRequest legacyEndpointDeprecation() {
        return content(
                "Turn off the five v1 endpoints with zero traffic and publish the sunset date for the rest.",
                "Every v1 endpoint we keep alive is a reason the monolith can't be switched off.",
                "https://github.com/acme/platform/pull/515",
                List.of(
                        task("Add request logging with caller identity to every v1 endpoint", "HIGH", 100, 100, "DONE", 6, 6,
                                "30 days of caller data collected"),
                        task("Remove the 5 endpoints with no calls in 30 days", "HIGH", 100, 100, "DONE", 5, 4,
                                "Endpoints removed; no incidents"),
                        task("Email remaining callers with a sunset date and migration guide", "MEDIUM", 80, 50, "IN_PROGRESS", 5, 4, null)),
                List.of(),
                List.of(
                        achievement("v1 surface down from 41 endpoints to 36 with more scheduled", false),
                        achievement("Now know exactly which team owns each remaining v1 caller", true)),
                List.of(hrs(DEV, "7.0"), hrs(TEST, "3.0"), hrs(MEET, "4.0"), hrs(DOCS, "6.0")));
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
