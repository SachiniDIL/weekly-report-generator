"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import type { ProjectResponse } from "@/lib/api/projects";
import {
  toReportContentRequest,
  validateReportContentForm,
  type ReportContentForm,
  type ReportIdentityDraft,
} from "./report-content-form";
import {
  AchievementsFieldset,
  BlockersFieldset,
  HoursFieldset,
  TaskEntriesFieldset,
} from "./report-editor-fieldsets";
import {
  useSaveReportDraftMutation,
  useSubmitReportMutation,
  type ReportEditorValues,
} from "./use-report-editor-mutations";
import { useReportContentForm } from "./use-report-content-form";

type IdentityProps =
  | { mode: "create"; projects: ProjectResponse[] }
  | { mode: "existing"; projectName: string; weekStart: string; weekEnd: string };

export function ReportEditorForm({
  reportId,
  identity,
  initialContent,
}: {
  reportId: number | null;
  identity: IdentityProps;
  initialContent: ReportContentForm;
}) {
  const content = useReportContentForm(initialContent);
  const [draftIdentity, setDraftIdentity] = useState<ReportIdentityDraft>({
    projectId: null,
    weekStart: "",
    weekEnd: "",
  });
  const [problems, setProblems] = useState<string[]>([]);
  const [confirmingSubmit, setConfirmingSubmit] = useState(false);

  const saveDraft = useSaveReportDraftMutation(reportId);
  const submit = useSubmitReportMutation(reportId);
  const pending = saveDraft.isPending || submit.isPending;
  const serverError = saveDraft.error ?? submit.error;
  const isCreate = identity.mode === "create";

  function currentValues(): ReportEditorValues {
    return {
      projectId: isCreate ? draftIdentity.projectId : null,
      weekStart: isCreate ? draftIdentity.weekStart : "",
      weekEnd: isCreate ? draftIdentity.weekEnd : "",
      content: toReportContentRequest(content.form),
    };
  }

  function passesValidation(): boolean {
    const found = validateReportContentForm(content.form, isCreate ? draftIdentity : undefined);
    setProblems(found);
    return found.length === 0;
  }

  function handleSaveDraft() {
    if (passesValidation()) {
      saveDraft.mutate(currentValues());
    }
  }

  function handleConfirmSubmit() {
    setConfirmingSubmit(false);
    if (passesValidation()) {
      submit.mutate(currentValues());
    }
  }

  return (
    <form className="flex flex-col gap-6" onSubmit={(event) => event.preventDefault()}>
      {identity.mode === "create" ? (
        <CreateIdentityFields
          projects={identity.projects}
          value={draftIdentity}
          onChange={setDraftIdentity}
        />
      ) : (
        <ReadOnlyIdentity
          projectName={identity.projectName}
          weekStart={identity.weekStart}
          weekEnd={identity.weekEnd}
        />
      )}

      <TextArea
        id="report-tasks-planned-next"
        label="Planned for next week"
        value={content.form.tasksPlannedNext}
        onChange={(value) => content.setText("tasksPlannedNext", value)}
      />
      <TextArea
        id="report-notes"
        label="Notes"
        value={content.form.notes}
        onChange={(value) => content.setText("notes", value)}
      />
      <Field
        id="report-links"
        label="Links"
        value={content.form.links}
        onChange={(value) => content.setText("links", value)}
      />

      <TaskEntriesFieldset controls={content.taskEntries} />
      <BlockersFieldset controls={content.blockers} />
      <AchievementsFieldset controls={content.achievements} />
      <HoursFieldset controls={content.hours} />

      {problems.length > 0 ? (
        <ul role="alert" className="rounded bg-red-50 p-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {problems.map((problem) => (
            <li key={problem}>{problem}</li>
          ))}
        </ul>
      ) : null}

      {serverError ? (
        <p role="alert" className="rounded bg-red-50 p-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
          {describeError(serverError)}
        </p>
      ) : null}

      {saveDraft.isSuccess && reportId != null ? (
        <p role="status" className="text-sm text-green-700 dark:text-green-400">
          Draft saved.
        </p>
      ) : null}

      <div className="flex flex-wrap gap-3">
        <button
          type="button"
          onClick={handleSaveDraft}
          disabled={pending}
          className="rounded border border-black/20 px-4 py-2 text-sm font-medium disabled:opacity-60 dark:border-white/25"
        >
          {saveDraft.isPending ? "Saving…" : "Save draft"}
        </button>
        <button
          type="button"
          onClick={() => setConfirmingSubmit(true)}
          disabled={pending}
          className="rounded bg-foreground px-4 py-2 text-sm font-medium text-background disabled:opacity-60"
        >
          Submit for review
        </button>
      </div>

      {confirmingSubmit ? (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Confirm submit"
          className="rounded border border-black/20 p-4 text-sm dark:border-white/25"
        >
          <p>
            Once submitted, a manager reviews this report and you can&apos;t edit it until they
            respond. Submit now?
          </p>
          <div className="mt-3 flex gap-3">
            <button
              type="button"
              onClick={() => setConfirmingSubmit(false)}
              className="rounded border border-black/20 px-3 py-1.5 dark:border-white/25"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleConfirmSubmit}
              className="rounded bg-foreground px-3 py-1.5 font-medium text-background"
            >
              {submit.isPending ? "Submitting…" : "Yes, submit"}
            </button>
          </div>
        </div>
      ) : null}
    </form>
  );
}

function CreateIdentityFields({
  projects,
  value,
  onChange,
}: {
  projects: ProjectResponse[];
  value: ReportIdentityDraft;
  onChange: (next: ReportIdentityDraft) => void;
}) {
  return (
    <div className="grid gap-4 sm:grid-cols-3">
      <div className="flex flex-col gap-1">
        <label htmlFor="report-project" className="text-sm font-medium">
          Project
        </label>
        <select
          id="report-project"
          value={value.projectId ?? ""}
          onChange={(event) =>
            onChange({ ...value, projectId: event.target.value ? Number(event.target.value) : null })
          }
          className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20"
        >
          <option value="">Select a project</option>
          {projects.map((project) => (
            <option key={project.id} value={project.id}>
              {project.name}
            </option>
          ))}
        </select>
      </div>
      <DateField
        id="report-week-start"
        label="Week start"
        value={value.weekStart}
        onChange={(weekStart) => onChange({ ...value, weekStart })}
      />
      <DateField
        id="report-week-end"
        label="Week end"
        value={value.weekEnd}
        onChange={(weekEnd) => onChange({ ...value, weekEnd })}
      />
    </div>
  );
}

function ReadOnlyIdentity({
  projectName,
  weekStart,
  weekEnd,
}: {
  projectName: string;
  weekStart: string;
  weekEnd: string;
}) {
  return (
    <dl className="grid gap-2 text-sm sm:grid-cols-3">
      <div>
        <dt className="text-gray-500">Project</dt>
        <dd>{projectName}</dd>
      </div>
      <div>
        <dt className="text-gray-500">Week start</dt>
        <dd>{weekStart}</dd>
      </div>
      <div>
        <dt className="text-gray-500">Week end</dt>
        <dd>{weekEnd}</dd>
      </div>
    </dl>
  );
}

function Field({
  id,
  label,
  value,
  onChange,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-medium">
        {label}
      </label>
      <input
        id={id}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm outline-none focus:border-black/40 dark:border-white/20"
      />
    </div>
  );
}

function DateField({
  id,
  label,
  value,
  onChange,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-medium">
        {label}
      </label>
      <input
        id={id}
        type="date"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20"
      />
    </div>
  );
}

function TextArea({
  id,
  label,
  value,
  onChange,
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-medium">
        {label}
      </label>
      <textarea
        id={id}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        rows={3}
        className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm outline-none focus:border-black/40 dark:border-white/20"
      />
    </div>
  );
}
