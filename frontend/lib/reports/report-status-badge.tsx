import type { ReportStatus } from "@/lib/api/reports";

const BADGE: Record<ReportStatus, { label: string; className: string }> = {
  DRAFT: {
    label: "Draft",
    className: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300",
  },
  SUBMITTED: {
    label: "Submitted",
    className: "bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300",
  },
  NEEDS_CORRECTION: {
    label: "Needs correction",
    className: "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300",
  },
  APPROVED: {
    label: "Approved",
    className: "bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-300",
  },
};

export function ReportStatusBadge({ status }: { status: ReportStatus }) {
  const badge = BADGE[status];
  return (
    <span className={`inline-block rounded px-2 py-0.5 text-xs font-medium ${badge.className}`}>
      {badge.label}
    </span>
  );
}
