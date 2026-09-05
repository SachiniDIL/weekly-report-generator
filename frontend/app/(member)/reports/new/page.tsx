"use client";

import { ReportMessage } from "@/lib/reports/report-message";
import { useProjectOptionsQuery } from "@/lib/reports/use-project-options-query";
import { emptyReportContentForm } from "../report-content-form";
import { ReportEditorForm } from "../report-editor-form";

export default function NewReportPage() {
  const projects = useProjectOptionsQuery();

  if (projects.isPending) {
    return <ReportMessage>Loading…</ReportMessage>;
  }
  if (projects.isError) {
    return <ReportMessage tone="error">Couldn&apos;t load the project list.</ReportMessage>;
  }

  return (
    <main className="mx-auto max-w-3xl p-6">
      <h1 className="mb-6 text-xl font-semibold">New weekly report</h1>
      <ReportEditorForm
        reportId={null}
        identity={{ mode: "create", projects: projects.data }}
        initialContent={emptyReportContentForm()}
      />
    </main>
  );
}
