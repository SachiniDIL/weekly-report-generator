"use client";

import { useReportVersionHistoryQuery } from "./use-report-version-history-query";
import { VersionHistoryList } from "./version-history-list";

/**
 * "Previous versions" — every version before the current one, each with the review comment made
 * against it. Renders nothing when the report has only ever had one version. Available to a
 * member on their own report and to any manager.
 */
export function PriorVersionsSection({
  reportId,
  enabled = true,
}: {
  reportId: number;
  enabled?: boolean;
}) {
  const history = useReportVersionHistoryQuery(reportId, enabled);
  const priorVersions = (history.data ?? []).slice(1);

  if (priorVersions.length === 0) {
    return null;
  }

  return (
    <section className="flex flex-col gap-3" aria-label="Previous versions">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-dusk-secondary">
        Previous versions
      </h2>
      <VersionHistoryList versions={priorVersions} />
    </section>
  );
}
