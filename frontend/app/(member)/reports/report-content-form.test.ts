import type { ReportContentResponse } from "@/lib/api/reports";
import {
  emptyReportContentForm,
  reportContentFormFromResponse,
  setExclusiveFlag,
  toReportContentRequest,
  validateReportContentForm,
  type BlockerRow,
} from "./report-content-form";

function blockers(...checked: boolean[]): BlockerRow[] {
  return checked.map((isKeyIssue, index) => ({
    key: `k${index}`,
    description: `b${index}`,
    isKeyIssue,
  }));
}

describe("setExclusiveFlag", () => {
  it("checking a row clears the flag on every other row", () => {
    const result = setExclusiveFlag(blockers(false, false, false), "k1", "isKeyIssue", true);
    expect(result.map((row) => row.isKeyIssue)).toEqual([false, true, false]);
  });

  it("moving the flag to a different row unchecks the previous one", () => {
    const first = setExclusiveFlag(blockers(false, true, false), "k0", "isKeyIssue", true);
    expect(first.map((row) => row.isKeyIssue)).toEqual([true, false, false]);
  });

  it("unchecking a row leaves the others untouched", () => {
    const result = setExclusiveFlag(blockers(true, false, false), "k0", "isKeyIssue", false);
    expect(result.map((row) => row.isKeyIssue)).toEqual([false, false, false]);
  });
});

describe("toReportContentRequest", () => {
  it("trims text, blanks become null, and numeric strings parse", () => {
    const form = emptyReportContentForm();
    form.notes = "  progressing  ";
    form.links = "   ";
    form.taskEntries = [
      {
        key: "t0",
        taskName: "  Design ",
        priority: "HIGH",
        plannedPct: "80",
        actualPct: "",
        status: "WIP",
        timePlanned: "6",
        timeSpent: "",
        deliverable: "",
      },
    ];
    form.hours = [{ key: "h0", taskType: "DEV", hours: "4.5" }];

    const request = toReportContentRequest(form);

    expect(request.notes).toBe("progressing");
    expect(request.links).toBeNull();
    expect(request.taskEntries?.[0]).toMatchObject({
      taskName: "Design",
      plannedPct: 80,
      actualPct: 0,
      timePlanned: 6,
      timeSpent: null,
      deliverable: null,
    });
    expect(request.hoursBreakdown?.[0]).toEqual({ taskType: "DEV", hours: 4.5 });
  });
});

describe("reportContentFormFromResponse", () => {
  it("round-trips a loaded report's content back into a request payload", () => {
    const content: ReportContentResponse = {
      reportVersionId: 1,
      versionNo: 1,
      submittedAt: "2026-09-05T00:00:00Z",
      tasksPlannedNext: "next things",
      notes: null,
      links: "https://example.com",
      taskEntries: [
        {
          id: 10,
          taskName: "Task",
          priority: "LOW",
          plannedPct: 100,
          actualPct: 100,
          status: "DONE",
          timePlanned: null,
          timeSpent: 3,
          deliverable: "doc",
        },
      ],
      blockers: [{ id: 20, description: "waiting", isKeyIssue: true }],
      achievements: [{ id: 30, description: "shipped", isKeyHighlight: false }],
      hoursBreakdown: [{ id: 40, taskType: "REVIEW", hours: 2 }],
    };

    const request = toReportContentRequest(reportContentFormFromResponse(content));

    expect(request.tasksPlannedNext).toBe("next things");
    expect(request.notes).toBeNull();
    expect(request.taskEntries?.[0]).toMatchObject({ taskName: "Task", timePlanned: null, timeSpent: 3 });
    expect(request.blockers).toEqual([{ description: "waiting", isKeyIssue: true }]);
    expect(request.hoursBreakdown).toEqual([{ taskType: "REVIEW", hours: 2 }]);
  });
});

describe("validateReportContentForm", () => {
  it("flags an incomplete task entry", () => {
    const form = emptyReportContentForm();
    form.taskEntries = [
      {
        key: "t0",
        taskName: "",
        priority: "",
        plannedPct: "",
        actualPct: "",
        status: "",
        timePlanned: "",
        timeSpent: "",
        deliverable: "",
      },
    ];
    expect(validateReportContentForm(form)).toEqual(["Task 1 needs a name, priority, and status."]);
  });

  it("flags missing identity fields in create mode", () => {
    const problems = validateReportContentForm(emptyReportContentForm(), {
      projectId: null,
      weekStart: "",
      weekEnd: "",
    });
    expect(problems).toEqual([
      "Choose a project.",
      "Set the week start date.",
      "Set the week end date.",
    ]);
  });

  it("returns nothing for a valid create form", () => {
    expect(
      validateReportContentForm(emptyReportContentForm(), {
        projectId: 1,
        weekStart: "2026-09-01",
        weekEnd: "2026-09-05",
      }),
    ).toEqual([]);
  });
});
