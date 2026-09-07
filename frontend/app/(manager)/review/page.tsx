"use client";

import Link from "next/link";
import { describeError } from "@/lib/api-client";
import { Avatar } from "@/lib/avatar";
import { ReportStatusBadge } from "@/lib/reports/report-status-badge";
import { useReviewQueueQuery } from "@/lib/reports/use-review-queue-query";
import { SkeletonText } from "@/lib/skeleton";

export default function ReviewQueuePage() {
  const query = useReviewQueueQuery();

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-6">
      <h1 className="text-xl font-semibold text-dusk-primary">Review queue</h1>

      {query.isPending ? <SkeletonText lines={5} /> : null}
      {query.isError ? (
        <p role="alert" className="text-sm text-red-700">
          {describeError(query.error)}
        </p>
      ) : null}

      {query.isSuccess && query.data.content.length === 0 ? (
        <p className="text-sm text-dusk-secondary">
          Nothing is waiting for review right now.
        </p>
      ) : null}

      {query.isSuccess && query.data.content.length > 0 ? (
        <ul className="dusk-panel divide-y divide-black/10 p-2">
          {query.data.content.map((report) => (
            <li key={report.id}>
              <Link
                href={`/reports/${report.id}/review`}
                className="dusk-row flex flex-wrap items-center justify-between gap-2 px-2 py-3 text-sm"
              >
                <span className="flex items-center gap-2">
                  <Avatar name={report.ownerName} size={24} />
                  <span className="flex flex-col">
                    <span className="font-medium text-dusk-primary">
                      {report.ownerName}
                    </span>
                    <span className="text-xs text-dusk-muted">
                      {report.projectName} · {report.weekStart} –{" "}
                      {report.weekEnd}
                    </span>
                  </span>
                </span>
                <ReportStatusBadge status={report.status} />
              </Link>
            </li>
          ))}
        </ul>
      ) : null}
    </main>
  );
}
