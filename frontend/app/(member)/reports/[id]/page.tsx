"use client";

import { useParams } from "next/navigation";
import { describeError } from "@/lib/api-client";
import { BackLink } from "@/lib/back-link";
import { CorrectionNotice } from "@/lib/reports/correction-notice";
import { findCorrectionComment } from "@/lib/reports/find-correction-comment";
import { PriorVersionsSection } from "@/lib/reports/prior-versions-section";
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

  if (!EDITABLE_STATUSES.includes(data.status)) {
    return (
      <main className="mx-auto flex w-full max-w-3xl flex-col gap-6">
        <BackLink href="/reports" label="My Reports" />
        <ReportContentView report={data} />
        <PriorVersionsSection
          reportId={reportId}
          enabled={data.currentVersionNo > 1}
        />
      </main>
    );
  }

  const correctionComment =
    needsCorrection && history.data
      ? findCorrectionComment(history.data)
      : null;

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-4">
      <BackLink href="/reports" label="My Reports" />
      <h1 className="text-xl font-semibold text-dusk-primary">
        {data.projectName} — week of {data.weekStart}
      </h1>
      {correctionComment ? (
        <CorrectionNotice comment={correctionComment} />
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
      <PriorVersionsSection
        reportId={reportId}
        enabled={data.currentVersionNo > 1}
      />
    </main>
  );
}
