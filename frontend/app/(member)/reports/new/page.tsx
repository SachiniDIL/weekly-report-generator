"use client";

import { BackLink } from "@/lib/back-link";
import { ReportMessage } from "@/lib/reports/report-message";
import { useProjectOptionsQuery } from "@/lib/reports/use-project-options-query";
import { emptyReportContentForm } from "../report-content-form";
import { ReportEditorForm } from "../report-editor-form";

export default function NewReportPage() {
  const projects = useProjectOptionsQuery();

  if (projects.isPending) {
    return <ReportMessage tone="loading" />;
  }
  if (projects.isError) {
    return (
      <ReportMessage tone="error">
        Couldn&apos;t load the project list.
      </ReportMessage>
    );
  }

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-4">
      <BackLink href="/reports" label="My Reports" />
      <h1 className="text-xl font-semibold text-dusk-primary">
        New weekly report
      </h1>
      <ReportEditorForm
        reportId={null}
        identity={{ mode: "create", projects: projects.data }}
        initialContent={emptyReportContentForm()}
      />
    </main>
  );
}
