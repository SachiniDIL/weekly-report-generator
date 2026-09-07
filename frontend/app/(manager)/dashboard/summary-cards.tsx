"use client";

import { describeError } from "@/lib/api-client";
import { useDashboardSummaryQuery } from "@/lib/dashboard/dashboard-queries";
import { Skeleton } from "@/lib/skeleton";

export function SummaryCards() {
  const query = useDashboardSummaryQuery();

  if (query.isPending) {
    return (
      <div
        role="status"
        aria-label="Loading summary"
        className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4"
      >
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} className="h-[76px]" />
        ))}
      </div>
    );
  }
  if (query.isError) {
    return (
      <p role="alert" className="text-sm text-red-700">
        {describeError(query.error)}
      </p>
    );
  }

  const summary = query.data;
  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <MetricCard
        label="Submitted this week"
        value={summary.totalSubmittedThisWeek}
        accent="#4f6ef7"
      />
      <MetricCard
        label="Compliance rate"
        value={`${Math.round(summary.complianceRate * 100)}%`}
        accent="#2f9e5f"
      />
      <MetricCard
        label="Needs correction"
        value={summary.needsCorrectionCount}
        accent="#e07b1a"
      />
      <MetricCard
        label="Open blockers"
        value={summary.openBlockersCount}
        accent="#dc4c4c"
      />
    </div>
  );
}

function MetricCard({
  label,
  value,
  accent,
}: {
  label: string;
  value: string | number;
  accent: string;
}) {
  return (
    <div
      className="dusk-panel p-4"
      style={{ borderLeft: `3px solid ${accent}` }}
    >
      <p className="text-xs uppercase tracking-wide text-dusk-secondary">
        {label}
      </p>
      <p className="mt-1 text-2xl font-semibold text-dusk-primary">{value}</p>
    </div>
  );
}
