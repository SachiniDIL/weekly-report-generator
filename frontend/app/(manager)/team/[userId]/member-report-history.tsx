"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import type { ReportListItemView } from "@/lib/api/reports";
import { PaginationControls } from "@/lib/reports/pagination-controls";
import { ReportMessage } from "@/lib/reports/report-message";
import { ReportsHistoryList } from "@/lib/reports/reports-history-list";
import { useMemberReportsQuery } from "@/lib/reports/use-member-reports-query";

const reviewHref = (report: ReportListItemView) => `/reports/${report.id}/review`;

export function MemberReportHistory({ userId }: { userId: number }) {
  const [page, setPage] = useState(0);
  const query = useMemberReportsQuery(userId, page);

  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-lg font-semibold">Report history</h2>

      {query.isPending ? <ReportMessage>Loading reports…</ReportMessage> : null}
      {query.isError ? (
        <ReportMessage tone="error">{describeError(query.error)}</ReportMessage>
      ) : null}

      {query.isSuccess && query.data.empty ? (
        <p className="rounded border border-dashed border-black/20 p-6 text-center text-sm text-gray-500 dark:border-white/25">
          This member hasn&apos;t written any reports yet.
        </p>
      ) : null}

      {query.isSuccess && !query.data.empty ? (
        <>
          <ReportsHistoryList reports={query.data.content} hrefForReport={reviewHref} />
          <PaginationControls
            pageNumber={query.data.number}
            totalPages={query.data.totalPages}
            onPrevious={() => setPage((current) => Math.max(0, current - 1))}
            onNext={() => setPage((current) => current + 1)}
          />
        </>
      ) : null}
    </section>
  );
}
