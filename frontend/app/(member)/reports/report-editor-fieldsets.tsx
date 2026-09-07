"use client";

import {
  AlertTriangle,
  CheckSquare,
  Clock,
  Plus,
  Star,
  type LucideIcon,
} from "lucide-react";
import type {
  AchievementRow,
  BlockerRow,
  HoursRow,
  TaskEntryRow,
} from "./report-content-form";
import type {
  FlaggedRowListControls,
  RowListControls,
} from "./use-report-content-form";

function DynamicList<Row extends { key: string }>({
  legend,
  icon: Icon,
  addLabel,
  rows,
  onAdd,
  onRemove,
  renderRow,
}: {
  legend: string;
  icon: LucideIcon;
  addLabel: string;
  rows: Row[];
  onAdd: () => void;
  onRemove: (key: string) => void;
  renderRow: (row: Row) => React.ReactNode;
}) {
  return (
    <section className="flex flex-col gap-3" aria-label={legend}>
      <h2 className="dusk-section-heading">
        <Icon size={14} aria-hidden />
        {legend}
      </h2>

      {rows.length === 0 ? (
        <p className="text-xs text-dusk-muted">None yet.</p>
      ) : null}

      {rows.map((row) => (
        <div
          key={row.key}
          className="flex flex-col gap-2 border-b border-black/5 pb-3 last:border-0"
        >
          {renderRow(row)}
          <button
            type="button"
            onClick={() => onRemove(row.key)}
            className="self-start text-xs text-red-600 hover:underline"
          >
            Remove
          </button>
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

function Cell({
  label,
  className = "",
  ...inputProps
}: {
  label: string;
  className?: string;
} & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className={`flex flex-col gap-1 ${className}`}>
      <span className="text-xs font-medium uppercase tracking-wide text-dusk-secondary">
        {label}
      </span>
      <input {...inputProps} className="px-2 py-1 text-sm" />
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
      legend="Task entries"
      icon={CheckSquare}
      addLabel="Add task"
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4 lg:grid-cols-8">
          <Cell
            label="Task name"
            className="col-span-2"
            value={row.taskName}
            onChange={(event) =>
              controls.update(row.key, { taskName: event.target.value })
            }
          />
          <Cell
            label="Priority"
            value={row.priority}
            onChange={(event) =>
              controls.update(row.key, { priority: event.target.value })
            }
          />
          <Cell
            label="Status"
            value={row.status}
            onChange={(event) =>
              controls.update(row.key, { status: event.target.value })
            }
          />
          <Cell
            label="Deliverable"
            className="col-span-2"
            value={row.deliverable}
            onChange={(event) =>
              controls.update(row.key, { deliverable: event.target.value })
            }
          />
          <Cell
            label="Planned %"
            type="number"
            value={row.plannedPct}
            onChange={(event) =>
              controls.update(row.key, { plannedPct: event.target.value })
            }
          />
          <Cell
            label="Actual %"
            type="number"
            value={row.actualPct}
            onChange={(event) =>
              controls.update(row.key, { actualPct: event.target.value })
            }
          />
          <Cell
            label="Time planned"
            type="number"
            value={row.timePlanned}
            onChange={(event) =>
              controls.update(row.key, { timePlanned: event.target.value })
            }
          />
          <Cell
            label="Time spent"
            type="number"
            value={row.timeSpent}
            onChange={(event) =>
              controls.update(row.key, { timeSpent: event.target.value })
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
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <Cell
            label="Description"
            className="sm:flex-1"
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
            Key issue
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
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <Cell
            label="Description"
            className="sm:flex-1"
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
            Key highlight
          </label>
        </div>
      )}
    />
  );
}

export function HoursFieldset({
  controls,
}: {
  controls: RowListControls<HoursRow>;
}) {
  return (
    <DynamicList
      legend="Hours breakdown"
      icon={Clock}
      addLabel="Add hours row"
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <Cell
            label="Task type"
            className="sm:flex-1"
            value={row.taskType}
            onChange={(event) =>
              controls.update(row.key, { taskType: event.target.value })
            }
          />
          <Cell
            label="Hours"
            type="number"
            value={row.hours}
            onChange={(event) =>
              controls.update(row.key, { hours: event.target.value })
            }
          />
        </div>
      )}
    />
  );
}
