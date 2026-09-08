"use client";

import { useParams } from "next/navigation";
import { describeError } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";
import { BackButton, BackLink } from "@/lib/back-link";
import { PriorVersionsSection } from "@/lib/reports/prior-versions-section";
import { isReportInaccessible } from "@/lib/reports/report-access";
import { ReportContentView } from "@/lib/reports/report-content-view";
import { ReportMessage } from "@/lib/reports/report-message";
import { useReportDetailQuery } from "@/lib/reports/use-report-detail-query";
import { useRequireAuth } from "@/lib/use-require-auth";

export default function ReportViewPage() {
  // Both roles can reach this route; the backend decides who may see this specific report.
  const { isChecking } = useRequireAuth({
    allowedRoles: ["MEMBER", "MANAGER"],
  });
  const { user } = useAuth();
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const query = useReportDetailQuery(reportId);

  if (isChecking) {
    return null;
  }

  if (
    !Number.isFinite(reportId) ||
    (query.isError && isReportInaccessible(query.error))
  ) {
    return (
      <ReportMessage>
        This report was not found, or you don&apos;t have access to it.
      </ReportMessage>
    );
  }
  if (query.isError) {
    return (
      <ReportMessage tone="error">{describeError(query.error)}</ReportMessage>
    );
  }
  if (query.isPending) {
    return <ReportMessage tone="loading" />;
  }

  return (
    <main className="mx-auto flex w-full max-w-2xl flex-col gap-6 p-6 md:p-8">
      {user?.role === "MANAGER" ? (
        <BackButton />
      ) : (
        <BackLink href="/reports" label="My Reports" />
      )}
      <ReportContentView report={query.data} />
      <PriorVersionsSection
        reportId={reportId}
        enabled={query.data.currentVersionNo > 1}
      />
    </main>
  );
}
