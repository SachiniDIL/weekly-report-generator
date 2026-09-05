"use client";

import { useParams } from "next/navigation";
import { describeError } from "@/lib/api-client";
import { CorrectionNotice } from "@/lib/reports/correction-notice";
import { findCorrectionComment } from "@/lib/reports/find-correction-comment";
import { isReportInaccessible } from "@/lib/reports/report-access";
import { ReportContentView } from "@/lib/reports/report-content-view";
import { ReportMessage } from "@/lib/reports/report-message";
import { useReportDetailQuery } from "@/lib/reports/use-report-detail-query";
import { useReportVersionHistoryQuery } from "@/lib/reports/use-report-version-history-query";
import { reportContentFormFromResponse } from "../report-content-form";
import { ReportEditorForm } from "../report-editor-form";

const EDITABLE_STATUSES: readonly string[] = ["DRAFT", "NEEDS_CORRECTION"];

export default function EditReportPage() {
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const report = useReportDetailQuery(reportId);

  const needsCorrection = report.data?.status === "NEEDS_CORRECTION";
  const history = useReportVersionHistoryQuery(reportId, needsCorrection);

  if (!Number.isFinite(reportId) || (report.isError && isReportInaccessible(report.error))) {
    return <ReportMessage>This report was not found, or you don&apos;t have access to it.</ReportMessage>;
  }
  if (report.isError) {
    return <ReportMessage tone="error">{describeError(report.error)}</ReportMessage>;
  }
  if (report.isPending) {
    return <ReportMessage>Loading report…</ReportMessage>;
  }

  const data = report.data;

  if (!EDITABLE_STATUSES.includes(data.status)) {
    return (
      <main className="mx-auto max-w-3xl p-6">
        <ReportContentView report={data} />
      </main>
    );
  }

  const correctionComment =
    needsCorrection && history.data ? findCorrectionComment(history.data) : null;

  return (
    <main className="mx-auto max-w-3xl p-6">
      <h1 className="mb-6 text-xl font-semibold">
        {data.projectName} — week of {data.weekStart}
      </h1>
      {correctionComment ? (
        <div className="mb-6">
          <CorrectionNotice comment={correctionComment} />
        </div>
      ) : null}
      <ReportEditorForm
        reportId={data.id}
        identity={{
          mode: "existing",
          projectName: data.projectName,
          weekStart: data.weekStart,
          weekEnd: data.weekEnd,
        }}
        initialContent={reportContentFormFromResponse(data.content)}
      />
    </main>
  );
}
