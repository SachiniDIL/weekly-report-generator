import type { ReportVersionHistoryItem, ReviewCommentView } from "@/lib/api/reports";
import { findCorrectionComment } from "./find-correction-comment";

const EMPTY_CONTENT = {
  reportVersionId: 0,
  versionNo: 0,
  submittedAt: "2026-09-05T00:00:00Z",
  tasksPlannedNext: null,
  notes: null,
  links: null,
  taskEntries: [],
  blockers: [],
  achievements: [],
  hoursBreakdown: [],
};

function item(reviewComment: ReviewCommentView | null): ReportVersionHistoryItem {
  return { content: EMPTY_CONTENT, reviewComment };
}

const CHANGES_REQUESTED: ReviewCommentView = {
  action: "CHANGES_REQUESTED",
  comment: "Please add the hours breakdown",
  managerName: "Dana",
  createdAt: "2026-09-06T00:00:00Z",
};

describe("findCorrectionComment", () => {
  it("returns the CHANGES_REQUESTED comment on the second-to-latest version", () => {
    const history = [item(null), item(CHANGES_REQUESTED)];
    expect(findCorrectionComment(history)).toBe(CHANGES_REQUESTED);
  });

  it("ignores an APPROVED comment on that version", () => {
    const history = [item(null), item({ ...CHANGES_REQUESTED, action: "APPROVED" })];
    expect(findCorrectionComment(history)).toBeNull();
  });

  it("returns null when the second-to-latest version has no comment", () => {
    expect(findCorrectionComment([item(null), item(null)])).toBeNull();
  });

  it("returns null when there is only one version", () => {
    expect(findCorrectionComment([item(CHANGES_REQUESTED)])).toBeNull();
  });

  it("returns null for empty history", () => {
    expect(findCorrectionComment([])).toBeNull();
  });
});
