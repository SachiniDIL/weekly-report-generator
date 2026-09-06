import type { MemberWeekStatus } from "@/lib/api/dashboard";

const STATUS_STYLE: Record<MemberWeekStatus, { label: string; className: string }> = {
  NOT_STARTED: {
    label: "Not started",
    className: "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300",
  },
  DRAFT: {
    label: "Draft",
    className: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-200",
  },
  NEEDS_CORRECTION: {
    label: "Needs correction",
    className: "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300",
  },
  SUBMITTED: {
    label: "Submitted",
    className: "bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300",
  },
  APPROVED: {
    label: "Approved",
    className: "bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-300",
  },
};

/** Shared status pill for the submission-status list and the section-comparison view. */
export function MemberWeekStatusChip({ status }: { status: MemberWeekStatus }) {
  const style = STATUS_STYLE[status];
  return (
    <span className={`rounded px-2 py-0.5 text-xs font-medium ${style.className}`}>
      {style.label}
    </span>
  );
}
