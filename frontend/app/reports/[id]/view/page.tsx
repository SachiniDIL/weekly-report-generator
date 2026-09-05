"use client";

import { useParams } from "next/navigation";
import { describeError } from "@/lib/api-client";
import { isReportInaccessible } from "@/lib/reports/report-access";
import { ReportContentView } from "@/lib/reports/report-content-view";
import { ReportMessage } from "@/lib/reports/report-message";
import { useReportDetailQuery } from "@/lib/reports/use-report-detail-query";
import { useRequireAuth } from "@/lib/use-require-auth";

export default function ReportViewPage() {
  // Both roles can reach this route; the backend decides who may see this specific report.
  const { isChecking } = useRequireAuth({ allowedRoles: ["MEMBER", "MANAGER"] });
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const query = useReportDetailQuery(reportId);

  if (isChecking) {
    return null;
  }

  if (!Number.isFinite(reportId) || (query.isError && isReportInaccessible(query.error))) {
    return (
      <ReportMessage>This report was not found, or you don&apos;t have access to it.</ReportMessage>
    );
  }
  if (query.isError) {
    return <ReportMessage tone="error">{describeError(query.error)}</ReportMessage>;
  }
  if (query.isPending) {
    return <ReportMessage>Loading report…</ReportMessage>;
  }

  return (
    <main className="mx-auto max-w-2xl p-6">
      <ReportContentView report={query.data} />
    </main>
  );
}
