"use client";

import { useParams } from "next/navigation";
import { describeError } from "@/lib/api-client";
import { isReportInaccessible } from "@/lib/reports/report-access";
import { ReportContentView } from "@/lib/reports/report-content-view";
import { ReportMessage } from "@/lib/reports/report-message";
import { useReportDetailQuery } from "@/lib/reports/use-report-detail-query";
import { useReportVersionHistoryQuery } from "@/lib/reports/use-report-version-history-query";
import { VersionHistoryList } from "@/lib/reports/version-history-list";
import { ReviewActionsForm } from "./review-actions-form";

export default function ReviewReportPage() {
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const report = useReportDetailQuery(reportId);
  const history = useReportVersionHistoryQuery(reportId);

  if (
    !Number.isFinite(reportId) ||
    (report.isError && isReportInaccessible(report.error))
  ) {
    return (
      <ReportMessage>
        This report was not found, or you don&apos;t have access to it.
      </ReportMessage>
    );
  }
  if (report.isError) {
    return (
      <ReportMessage tone="error">{describeError(report.error)}</ReportMessage>
    );
  }
  if (report.isPending) {
    return <ReportMessage tone="loading" />;
  }

  const data = report.data;
  const awaitingReview = data.status === "SUBMITTED";
  const priorVersions = (history.data ?? []).slice(1);

  return (
    <main className="mx-auto flex max-w-3xl flex-col gap-8 p-6">
      <ReportContentView report={data} />

      {awaitingReview ? (
        <ReviewActionsForm reportId={data.id} />
      ) : (
        <ReportMessage>
          This report is {data.status.replace("_", " ").toLowerCase()} — review
          actions are only available while it&apos;s awaiting review.
        </ReportMessage>
      )}

      {priorVersions.length > 0 ? (
        <section className="flex flex-col gap-4">
          <h2 className="text-lg font-semibold">Previous versions</h2>
          <VersionHistoryList versions={priorVersions} />
        </section>
      ) : null}
    </main>
  );
}
