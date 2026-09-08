"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import type { ProjectResponse } from "@/lib/api/projects";
import type { ReportStatus } from "@/lib/api/reports";
import { BackLink } from "@/lib/back-link";
import { ReportMessage } from "@/lib/reports/report-message";
import { ReportStatusBadge } from "@/lib/reports/report-status-badge";
import { recentWeeks } from "@/lib/reports/recent-weeks";
import {
  coverageKey,
  useMyReportCoverageQuery,
} from "@/lib/reports/use-my-report-coverage-query";
import { useProjectOptionsQuery } from "@/lib/reports/use-project-options-query";
import { emptyReportContentForm } from "../report-content-form";
import { ReportEditorForm } from "../report-editor-form";

const FIELD_LABEL =
  "text-xs font-medium uppercase tracking-wide text-dusk-secondary";

/** How many past weeks a member may still file a report for (plus the current one). */
const WEEK_CHOICES = 6;
/** How many weeks the "where you stand" table covers. */
const COVERAGE_WEEKS = 2;

interface ExistingReport {
  id: number;
  status: ReportStatus;
}

export default function NewReportPage() {
  const projects = useProjectOptionsQuery();
  const coverage = useMyReportCoverageQuery();

  const weeks = useMemo(() => recentWeeks(WEEK_CHOICES), []);
  const [weekIndex, setWeekIndex] = useState(0);
  const [projectId, setProjectId] = useState<number | null>(null);

  const existingByKey = useMemo(() => {
    const map = new Map<string, ExistingReport>();
    for (const report of coverage.data?.content ?? []) {
      map.set(coverageKey(report.projectId, report.weekStart), {
        id: report.id,
        status: report.status,
      });
    }
    return map;
  }, [coverage.data]);

  if (projects.isPending || coverage.isPending) {
    return <ReportMessage tone="loading" />;
  }
  if (projects.isError || coverage.isError) {
    return (
      <ReportMessage tone="error">
        Couldn&apos;t load your projects. Try again in a moment.
      </ReportMessage>
    );
  }

  const week = weeks[weekIndex];
  const chosenProject = projects.data.find((p) => p.id === projectId) ?? null;
  const existing =
    chosenProject != null
      ? (existingByKey.get(coverageKey(chosenProject.id, week.weekStart)) ??
        null)
      : null;

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-6">
      <BackLink href="/reports" label="My Reports" />
      <h1 className="text-xl font-semibold text-dusk-primary">
        New weekly report
      </h1>

      <CoverageTable
        projects={projects.data}
        weeks={weeks.slice(0, COVERAGE_WEEKS)}
        existingByKey={existingByKey}
        onStart={(pid, index) => {
          setProjectId(pid);
          setWeekIndex(index);
        }}
      />

      <section className="dusk-panel flex flex-col gap-4 p-4 sm:p-6">
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="flex flex-col gap-1">
            <label htmlFor="new-report-week" className={FIELD_LABEL}>
              Week
            </label>
            <select
              id="new-report-week"
              value={weekIndex}
              onChange={(event) => {
                // A project may be taken in one week but free in another, so re-pick.
                setProjectId(null);
                setWeekIndex(Number(event.target.value));
              }}
              className="px-3 py-2 text-sm"
            >
              {weeks.map((option, index) => (
                <option key={option.weekStart} value={index}>
                  {option.label} · {option.weekStart} – {option.weekEnd}
                </option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="new-report-project" className={FIELD_LABEL}>
              Project
            </label>
            <select
              id="new-report-project"
              value={projectId ?? ""}
              onChange={(event) =>
                setProjectId(
                  event.target.value ? Number(event.target.value) : null,
                )
              }
              className="px-3 py-2 text-sm"
            >
              <option value="">Select a project</option>
              {projects.data.map((project) => {
                const taken = existingByKey.has(
                  coverageKey(project.id, week.weekStart),
                );
                return (
                  <option key={project.id} value={project.id} disabled={taken}>
                    {project.name}
                    {taken ? " — already started" : ""}
                  </option>
                );
              })}
            </select>
          </div>
        </div>

        {chosenProject == null ? (
          <p className="text-sm text-dusk-muted">
            Choose a project and week to start filling in your report.
          </p>
        ) : existing != null ? (
          <p className="dusk-banner p-3 text-sm">
            You already have a <ReportStatusBadge status={existing.status} />{" "}
            report for {chosenProject.name} this period.{" "}
            <Link
              href={`/reports/${existing.id}`}
              className="text-dusk-accent-light underline"
            >
              Open it
            </Link>
            .
          </p>
        ) : null}
      </section>

      {chosenProject != null && existing == null ? (
        <ReportEditorForm
          reportId={null}
          identity={{
            mode: "create",
            projectId: chosenProject.id,
            projectName: chosenProject.name,
            weekStart: week.weekStart,
            weekEnd: week.weekEnd,
          }}
          initialContent={emptyReportContentForm()}
        />
      ) : null}
    </main>
  );
}

function CoverageTable({
  projects,
  weeks,
  existingByKey,
  onStart,
}: {
  projects: ProjectResponse[];
  weeks: ReturnType<typeof recentWeeks>;
  existingByKey: Map<string, ExistingReport>;
  onStart: (projectId: number, weekIndex: number) => void;
}) {
  if (projects.length === 0) {
    return (
      <p className="text-sm text-dusk-secondary">
        You aren&apos;t assigned to any projects yet.
      </p>
    );
  }

  return (
    <section
      className="dusk-panel flex flex-col gap-3 p-4 sm:p-5"
      aria-label="Your reports by project"
    >
      <h2 className="text-sm font-semibold uppercase tracking-wide text-dusk-secondary">
        Where you stand
      </h2>
      <div className="overflow-x-auto">
        <table className="w-full min-w-[420px] text-sm">
          <thead>
            <tr className="text-left text-xs uppercase tracking-wide text-dusk-muted">
              <th className="pb-2 pr-4 font-medium">Project</th>
              {weeks.map((week) => (
                <th key={week.weekStart} className="pb-2 pr-4 font-medium">
                  {week.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {projects.map((project) => (
              <tr
                key={project.id}
                className="border-t border-dusk-border/60 align-middle"
              >
                <td className="py-2 pr-4 text-dusk-primary">{project.name}</td>
                {weeks.map((week, index) => {
                  const existing = existingByKey.get(
                    coverageKey(project.id, week.weekStart),
                  );
                  return (
                    <td key={week.weekStart} className="py-2 pr-4">
                      {existing ? (
                        <Link
                          href={`/reports/${existing.id}`}
                          className="inline-flex items-center gap-1.5"
                        >
                          <ReportStatusBadge status={existing.status} />
                        </Link>
                      ) : (
                        <button
                          type="button"
                          onClick={() => onStart(project.id, index)}
                          className="text-xs text-dusk-accent-light underline"
                        >
                          Not started — start
                        </button>
                      )}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
