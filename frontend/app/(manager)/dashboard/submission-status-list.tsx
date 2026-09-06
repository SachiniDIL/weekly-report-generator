"use client";

import type { MemberWeekStatus } from "@/lib/api/dashboard";
import { useSubmissionStatusQuery } from "@/lib/dashboard/dashboard-queries";
import { ChartCard } from "./chart-card";

const STATUS_STYLE: Record<MemberWeekStatus, { label: string; className: string }> = {
  NOT_STARTED: { label: "Not started", className: "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300" },
  DRAFT: { label: "Draft", className: "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-200" },
  NEEDS_CORRECTION: { label: "Needs correction", className: "bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300" },
  SUBMITTED: { label: "Submitted", className: "bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300" },
  APPROVED: { label: "Approved", className: "bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-300" },
};

export function SubmissionStatusList() {
  const query = useSubmissionStatusQuery();

  return (
    <ChartCard title="Submission status this week" query={query}>
      {(rows) => (
        <ul className="flex flex-col divide-y divide-black/5 text-sm dark:divide-white/10">
          {rows.map((row) => (
            <li key={row.memberName} className="flex items-center justify-between gap-2 py-2">
              <span>{row.memberName}</span>
              <StatusChip status={row.status} />
            </li>
          ))}
        </ul>
      )}
    </ChartCard>
  );
}

function StatusChip({ status }: { status: MemberWeekStatus }) {
  const style = STATUS_STYLE[status];
  return (
    <span className={`rounded px-2 py-0.5 text-xs font-medium ${style.className}`}>{style.label}</span>
  );
}
