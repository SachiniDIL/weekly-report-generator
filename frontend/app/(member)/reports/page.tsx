"use client";

import Link from "next/link";
import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { ReportMessage } from "@/lib/reports/report-message";
import { PaginationControls } from "@/lib/reports/pagination-controls";
import { ReportsHistoryList } from "@/lib/reports/reports-history-list";
import { useMyReportsQuery } from "@/lib/reports/use-my-reports-query";

export default function ReportHistoryPage() {
  const [page, setPage] = useState(0);
  const query = useMyReportsQuery(page);

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-semibold text-dusk-primary">My reports</h1>
        <Link
          href="/reports/new"
          className="rounded-lg bg-foreground px-4 py-2 text-sm font-medium text-background"
        >
          Create new report
        </Link>
      </header>

      {query.isPending ? <ReportMessage tone="loading" /> : null}
      {query.isError ? (
        <ReportMessage tone="error">{describeError(query.error)}</ReportMessage>
      ) : null}

      {query.isSuccess && query.data.empty ? (
        <p className="rounded-xl border border-dashed border-white/20 p-6 text-center text-sm text-dusk-secondary">
          You haven&apos;t written any reports yet. Start with &ldquo;Create new
          report&rdquo;.
        </p>
      ) : null}

      {query.isSuccess && !query.data.empty ? (
        <>
          <ReportsHistoryList reports={query.data.content} />
          <PaginationControls
            pageNumber={query.data.number}
            totalPages={query.data.totalPages}
            onPrevious={() => setPage((current) => Math.max(0, current - 1))}
            onNext={() => setPage((current) => current + 1)}
          />
        </>
      ) : null}
    </main>
  );
}
