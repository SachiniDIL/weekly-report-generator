"use client";

import {
  AlertTriangle,
  CheckSquare,
  Clock,
  Plus,
  Star,
  Trash2,
  type LucideIcon,
} from "lucide-react";
import {
  TASK_PRIORITIES,
  TASK_STATUSES,
  type AchievementRow,
  type BlockerRow,
  type HoursRow,
  type TaskEntryRow,
} from "./report-content-form";
import type {
  FixedRowListControls,
  FlaggedRowListControls,
  RowListControls,
} from "./use-report-content-form";

const CELL_LABEL =
  "text-xs font-medium uppercase tracking-wide text-dusk-secondary";

function SectionHeading({
  icon: Icon,
  children,
}: {
  icon: LucideIcon;
  children: React.ReactNode;
}) {
  return (
    <h2 className="dusk-section-heading">
      <Icon size={14} aria-hidden />
      {children}
    </h2>
  );
}

function DynamicList<Row extends { key: string }>({
  legend,
  icon,
  addLabel,
  emptyLabel,
  rows,
  onAdd,
  onRemove,
  renderRow,
}: {
  legend: string;
  icon: LucideIcon;
  addLabel: string;
  emptyLabel: string;
  rows: Row[];
  onAdd: () => void;
  onRemove: (key: string) => void;
  renderRow: (row: Row, index: number) => React.ReactNode;
}) {
  return (
    <section className="flex flex-col gap-3" aria-label={legend}>
      <SectionHeading icon={icon}>{legend}</SectionHeading>

      {rows.length === 0 ? (
        <p className="text-xs text-dusk-muted">{emptyLabel}</p>
      ) : null}

      {rows.map((row, index) => (
        <div
          key={row.key}
          className="flex flex-col gap-3 rounded-xl bg-black/[0.03] p-3"
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-dusk-muted">
              {legend.replace(/s$/, "")} {index + 1}
            </span>
            <button
              type="button"
              onClick={() => onRemove(row.key)}
              aria-label={`Remove ${legend.replace(/s$/, "").toLowerCase()} ${index + 1}`}
              className="text-dusk-muted transition-colors hover:text-dusk-danger"
            >
              <Trash2 size={14} aria-hidden />
            </button>
          </div>
          {renderRow(row, index)}
        </div>
      ))}

      <button
        type="button"
        onClick={onAdd}
        className="dusk-ghost-btn self-start px-3 py-1.5 text-xs"
      >
        <Plus size={13} aria-hidden />
        {addLabel}
      </button>
    </section>
  );
}

function TextCell({
  label,
  className = "",
  ...inputProps
}: {
  label: string;
  className?: string;
} & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className={`flex flex-col gap-1 ${className}`}>
      <span className={CELL_LABEL}>{label}</span>
      <input {...inputProps} className="px-2 py-1.5 text-sm" />
    </label>
  );
}

function SelectCell({
  label,
  className = "",
  options,
  ...selectProps
}: {
  label: string;
  className?: string;
  options: ReadonlyArray<{ value: string; label: string }>;
} & React.SelectHTMLAttributes<HTMLSelectElement>) {
  return (
    <label className={`flex flex-col gap-1 ${className}`}>
      <span className={CELL_LABEL}>{label}</span>
      <select {...selectProps} className="px-2 py-1.5 text-sm">
        <option value="">Select…</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}

export function TaskEntriesFieldset({
  controls,
}: {
  controls: RowListControls<TaskEntryRow>;
}) {
  return (
    <DynamicList
      legend="Tasks"
      icon={CheckSquare}
      addLabel="Add task"
      emptyLabel="No tasks added yet."
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-3">
          <TextCell
            label="Task name"
            value={row.taskName}
            onChange={(event) =>
              controls.update(row.key, { taskName: event.target.value })
            }
          />
          <div className="grid grid-cols-2 gap-3">
            <SelectCell
              label="Priority"
              options={TASK_PRIORITIES}
              value={row.priority}
              onChange={(event) =>
                controls.update(row.key, { priority: event.target.value })
              }
            />
            <SelectCell
              label="Status"
              options={TASK_STATUSES}
              value={row.status}
              onChange={(event) =>
                controls.update(row.key, { status: event.target.value })
              }
            />
          </div>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <TextCell
              label="Planned %"
              type="number"
              min={0}
              max={100}
              value={row.plannedPct}
              onChange={(event) =>
                controls.update(row.key, { plannedPct: event.target.value })
              }
            />
            <TextCell
              label="Actual %"
              type="number"
              min={0}
              max={100}
              value={row.actualPct}
              onChange={(event) =>
                controls.update(row.key, { actualPct: event.target.value })
              }
            />
            <TextCell
              label="Time planned (h)"
              type="number"
              min={0}
              value={row.timePlanned}
              onChange={(event) =>
                controls.update(row.key, { timePlanned: event.target.value })
              }
            />
            <TextCell
              label="Time spent (h)"
              type="number"
              min={0}
              value={row.timeSpent}
              onChange={(event) =>
                controls.update(row.key, { timeSpent: event.target.value })
              }
            />
          </div>
          <TextCell
            label="Deliverable"
            value={row.deliverable}
            onChange={(event) =>
              controls.update(row.key, { deliverable: event.target.value })
            }
          />
        </div>
      )}
    />
  );
}

export function BlockersFieldset({
  controls,
}: {
  controls: FlaggedRowListControls<BlockerRow>;
}) {
  return (
    <DynamicList
      legend="Blockers"
      icon={AlertTriangle}
      addLabel="Add blocker"
      emptyLabel="No blockers this week."
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2">
          <TextCell
            label="Description"
            value={row.description}
            onChange={(event) =>
              controls.update(row.key, { description: event.target.value })
            }
          />
          <label className="flex items-center gap-2 text-xs text-dusk-secondary">
            <input
              type="checkbox"
              checked={row.isKeyIssue}
              onChange={(event) =>
                controls.setExclusive(row.key, event.target.checked)
              }
            />
            Mark as the key issue
          </label>
        </div>
      )}
    />
  );
}

export function AchievementsFieldset({
  controls,
}: {
  controls: FlaggedRowListControls<AchievementRow>;
}) {
  return (
    <DynamicList
      legend="Achievements"
      icon={Star}
      addLabel="Add achievement"
      emptyLabel="No achievements added yet."
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2">
          <TextCell
            label="Description"
            value={row.description}
            onChange={(event) =>
              controls.update(row.key, { description: event.target.value })
            }
          />
          <label className="flex items-center gap-2 text-xs text-dusk-secondary">
            <input
              type="checkbox"
              checked={row.isKeyHighlight}
              onChange={(event) =>
                controls.setExclusive(row.key, event.target.checked)
              }
            />
            Mark as the key highlight
          </label>
        </div>
      )}
    />
  );
}

export function HoursFieldset({
  controls,
}: {
  controls: FixedRowListControls<HoursRow>;
}) {
  return (
    <section className="flex flex-col gap-3" aria-label="Hours breakdown">
      <SectionHeading icon={Clock}>Hours breakdown</SectionHeading>
      <p className="text-xs text-dusk-muted">
        Hours spent on each type of work this week. Leave a field blank if it
        doesn&apos;t apply.
      </p>
      <div className="grid gap-3 sm:grid-cols-2">
        {controls.rows.map((row) => (
          <label
            key={row.key}
            className="flex items-center justify-between gap-3"
          >
            <span className="text-sm text-dusk-primary">{row.taskType}</span>
            <input
              type="number"
              min={0}
              step="0.5"
              aria-label={`Hours for ${row.taskType}`}
              value={row.hours}
              onChange={(event) =>
                controls.update(row.key, { hours: event.target.value })
              }
              className="w-24 px-2 py-1.5 text-sm"
            />
          </label>
        ))}
      </div>
    </section>
  );
}
