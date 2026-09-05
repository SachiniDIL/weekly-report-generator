"use client";

import { useParams } from "next/navigation";
import { ApiError, describeError } from "@/lib/api-client";
import { useRequireAuth } from "@/lib/use-require-auth";
import { useReportDetailQuery } from "./use-report-detail-query";

export default function ReportViewPage() {
  // Both roles can reach this route; the backend decides who may see this specific report.
  const { isChecking } = useRequireAuth({ allowedRoles: ["MEMBER", "MANAGER"] });
  const params = useParams<{ id: string }>();
  const reportId = Number(params.id);
  const query = useReportDetailQuery(reportId);

  if (isChecking) {
    return null;
  }

  if (!Number.isFinite(reportId) || (query.isError && isInaccessible(query.error))) {
    return (
      <Message tone="muted">This report was not found, or you don&apos;t have access to it.</Message>
    );
  }

  if (query.isError) {
    return <Message tone="error">{describeError(query.error)}</Message>;
  }

  if (query.isPending) {
    return <Message tone="muted">Loading report…</Message>;
  }

  const report = query.data;
  return (
    <main className="mx-auto max-w-2xl p-6">
      <h1 className="text-xl font-semibold">
        {report.projectName} — week of {report.weekStart}
      </h1>
      <dl className="mt-4 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
        <dt className="text-gray-500">Owner</dt>
        <dd>{report.ownerName}</dd>
        <dt className="text-gray-500">Status</dt>
        <dd>{report.status}</dd>
        <dt className="text-gray-500">Current version</dt>
        <dd>v{report.currentVersionNo}</dd>
      </dl>
    </main>
  );
}

function isInaccessible(error: unknown): boolean {
  return error instanceof ApiError && (error.status === 403 || error.status === 404);
}

function Message({ tone, children }: { tone: "muted" | "error"; children: React.ReactNode }) {
  return (
    <p
      className={`p-6 text-sm ${tone === "error" ? "text-red-700" : "text-gray-500"}`}
      role={tone === "error" ? "alert" : undefined}
    >
      {children}
    </p>
  );
}
