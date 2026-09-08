"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import type { ProjectResponse } from "@/lib/api/projects";
import { useConfirm } from "@/lib/confirm-dialog";
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
  | {
      mode: "existing";
      projectName: string;
      weekStart: string;
      weekEnd: string;
    };

const FIELD_LABEL =
  "text-xs font-medium uppercase tracking-wide text-dusk-secondary";

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
  const confirm = useConfirm();
  const [draftIdentity, setDraftIdentity] = useState<ReportIdentityDraft>({
    projectId: null,
    weekStart: "",
    weekEnd: "",
  });
  const [problems, setProblems] = useState<string[]>([]);

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
    const found = validateReportContentForm(
      content.form,
      isCreate ? draftIdentity : undefined,
    );
    setProblems(found);
    return found.length === 0;
  }

  function handleSaveDraft() {
    if (passesValidation()) {
      saveDraft.mutate(currentValues());
    }
  }

  async function handleSubmit() {
    if (!passesValidation()) {
      return;
    }
    const confirmed = await confirm({
      title: "Submit this report?",
      message:
        "Once submitted, a manager reviews it and you can't edit it until they respond.",
      confirmLabel: "Submit for review",
    });
    if (confirmed) {
      submit.mutate(currentValues());
    }
  }

  return (
    <form
      className="dusk-panel flex flex-col gap-6 p-6"
      onSubmit={(event) => event.preventDefault()}
    >
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

      <ActionBar
        onSaveDraft={handleSaveDraft}
        onSubmit={handleSubmit}
        savingDraft={saveDraft.isPending}
        submitting={submit.isPending}
        disabled={pending}
        showSubmit={false}
      />

      <div className="flex flex-col gap-6">
        <TaskEntriesFieldset controls={content.taskEntries} />
        <BlockersFieldset controls={content.blockers} />
        <AchievementsFieldset controls={content.achievements} />
        <HoursFieldset controls={content.hours} />

        <section className="flex flex-col gap-3" aria-label="Notes and links">
          <h2 className="dusk-section-heading">Notes &amp; links</h2>
          <div className="grid gap-4 sm:grid-cols-2">
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
          </div>
        </section>

        <section
          className="flex flex-col gap-3"
          aria-label="Planned for next week"
        >
          <h2 className="dusk-section-heading">Planned for next week</h2>
          <TextArea
            id="report-tasks-planned-next"
            label="What's planned for next week"
            value={content.form.tasksPlannedNext}
            onChange={(value) => content.setText("tasksPlannedNext", value)}
          />
        </section>
      </div>

      {problems.length > 0 ? (
        <ul
          role="alert"
          className="dusk-banner-error flex flex-col gap-1 p-3 text-sm"
        >
          {problems.map((problem) => (
            <li key={problem}>{problem}</li>
          ))}
        </ul>
      ) : null}

      {serverError ? (
        <p role="alert" className="dusk-banner-error p-3 text-sm">
          {describeError(serverError)}
        </p>
      ) : null}

      <ActionBar
        onSaveDraft={handleSaveDraft}
        onSubmit={handleSubmit}
        savingDraft={saveDraft.isPending}
        submitting={submit.isPending}
        disabled={pending}
        showSubmit
      />
    </form>
  );
}

function ActionBar({
  onSaveDraft,
  onSubmit,
  savingDraft,
  submitting,
  disabled,
  showSubmit,
}: {
  onSaveDraft: () => void;
  onSubmit: () => void;
  savingDraft: boolean;
  submitting: boolean;
  disabled: boolean;
  showSubmit: boolean;
}) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <button
        type="button"
        onClick={onSaveDraft}
        disabled={disabled}
        className="dusk-ghost-btn px-4 py-2 text-sm font-medium"
      >
        {savingDraft ? "Saving…" : "Save draft"}
      </button>
      {showSubmit ? (
        <button
          type="button"
          onClick={onSubmit}
          disabled={disabled}
          className="rounded-lg bg-foreground px-4 py-2 text-sm font-medium text-background disabled:opacity-60"
        >
          {submitting ? "Submitting…" : "Submit for review"}
        </button>
      ) : (
        <span className="text-xs text-dusk-muted">
          Your progress is kept once you save a draft.
        </span>
      )}
    </div>
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
    <div className="grid gap-4 sm:grid-cols-2">
      <div className="flex flex-col gap-1 sm:col-span-2">
        <label htmlFor="report-project" className={FIELD_LABEL}>
          Project
        </label>
        <select
          id="report-project"
          value={value.projectId ?? ""}
          onChange={(event) =>
            onChange({
              ...value,
              projectId: event.target.value ? Number(event.target.value) : null,
            })
          }
          className="px-3 py-2 text-sm"
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
    <dl className="grid gap-3 text-sm sm:grid-cols-3">
      <IdentityFact label="Project" value={projectName} />
      <IdentityFact label="Week start" value={weekStart} />
      <IdentityFact label="Week end" value={weekEnd} />
    </dl>
  );
}

function IdentityFact({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex flex-col gap-0.5">
      <dt className={FIELD_LABEL}>{label}</dt>
      <dd className="text-dusk-primary">{value}</dd>
    </div>
  );
}

function Field({
  id,
  label,
  value,
  onChange,
  className = "",
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  className?: string;
}) {
  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      <label htmlFor={id} className={FIELD_LABEL}>
        {label}
      </label>
      <input
        id={id}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="px-3 py-2 text-sm"
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
      <label htmlFor={id} className={FIELD_LABEL}>
        {label}
      </label>
      <input
        id={id}
        type="date"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="px-3 py-2 text-sm"
      />
    </div>
  );
}

function TextArea({
  id,
  label,
  value,
  onChange,
  className = "",
}: {
  id: string;
  label: string;
  value: string;
  onChange: (value: string) => void;
  className?: string;
}) {
  return (
    <div className={`flex flex-col gap-1 ${className}`}>
      <label htmlFor={id} className={FIELD_LABEL}>
        {label}
      </label>
      <textarea
        id={id}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        rows={3}
        className="px-3 py-2 text-sm"
      />
    </div>
  );
}
