"use client";

import { describeError } from "@/lib/api-client";
import { useDashboardSummaryQuery } from "@/lib/dashboard/dashboard-queries";

export function SummaryCards() {
  const query = useDashboardSummaryQuery();

  if (query.isPending) {
    return <p className="text-sm text-gray-500">Loading summary…</p>;
  }
  if (query.isError) {
    return (
      <p role="alert" className="text-sm text-red-700 dark:text-red-300">
        {describeError(query.error)}
      </p>
    );
  }

  const summary = query.data;
  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard label="Submitted this week" value={summary.totalSubmittedThisWeek} />
      <MetricCard label="Compliance rate" value={`${Math.round(summary.complianceRate * 100)}%`} />
      <MetricCard label="Needs correction" value={summary.needsCorrectionCount} />
      <MetricCard label="Open blockers" value={summary.openBlockersCount} />
    </div>
  );
}

function MetricCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="rounded border border-black/10 p-4 dark:border-white/15">
      <p className="text-xs uppercase tracking-wide text-gray-500">{label}</p>
      <p className="mt-1 text-2xl font-semibold">{value}</p>
    </div>
  );
}
