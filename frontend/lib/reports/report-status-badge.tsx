import type { ReportStatus } from "@/lib/api/reports";

const LABEL: Record<ReportStatus, string> = {
  DRAFT: "Draft",
  SUBMITTED: "Submitted",
  NEEDS_CORRECTION: "Needs correction",
  APPROVED: "Approved",
};

export function ReportStatusBadge({ status }: { status: ReportStatus }) {
  return (
    <span className="dusk-badge" data-status={status}>
      {LABEL[status]}
    </span>
  );
}
