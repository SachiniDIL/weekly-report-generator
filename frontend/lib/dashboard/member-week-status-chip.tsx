import type { MemberWeekStatus } from "@/lib/api/dashboard";

const LABEL: Record<MemberWeekStatus, string> = {
  NOT_STARTED: "Not started",
  DRAFT: "Draft",
  NEEDS_CORRECTION: "Needs correction",
  SUBMITTED: "Submitted",
  APPROVED: "Approved",
};

/** Shared status pill for the submission-status list and the section-comparison view. */
export function MemberWeekStatusChip({ status }: { status: MemberWeekStatus }) {
  return (
    <span className="dusk-badge" data-status={status}>
      {LABEL[status]}
    </span>
  );
}
