"use client";

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
  addLabel,
  rows,
  onAdd,
  onRemove,
  renderRow,
}: {
  legend: string;
  addLabel: string;
  rows: Row[];
  onAdd: () => void;
  onRemove: (key: string) => void;
  renderRow: (row: Row) => React.ReactNode;
}) {
  return (
    <fieldset className="flex flex-col gap-3 rounded border border-black/10 p-4 dark:border-white/15">
      <legend className="px-1 text-sm font-semibold">{legend}</legend>
      {rows.length === 0 ? <p className="text-sm text-gray-500">None yet.</p> : null}
      {rows.map((row) => (
        <div key={row.key} className="flex flex-col gap-2 border-b border-black/5 pb-3 last:border-0 dark:border-white/10">
          {renderRow(row)}
          <button
            type="button"
            onClick={() => onRemove(row.key)}
            className="self-start text-xs text-red-600 underline"
          >
            Remove
          </button>
        </div>
      ))}
      <button type="button" onClick={onAdd} className="self-start text-sm underline">
        {addLabel}
      </button>
    </fieldset>
  );
}

function Cell({
  label,
  ...inputProps
}: { label: string } & React.InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className="flex flex-col gap-1 text-xs">
      <span className="text-gray-500">{label}</span>
      <input
        {...inputProps}
        className="rounded border border-black/15 bg-transparent px-2 py-1 text-sm outline-none focus:border-black/40 dark:border-white/20"
      />
    </label>
  );
}

export function TaskEntriesFieldset({ controls }: { controls: RowListControls<TaskEntryRow> }) {
  return (
    <DynamicList
      legend="Task entries"
      addLabel="Add task"
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
          <Cell
            label="Task name"
            value={row.taskName}
            onChange={(event) => controls.update(row.key, { taskName: event.target.value })}
          />
          <Cell
            label="Priority"
            value={row.priority}
            onChange={(event) => controls.update(row.key, { priority: event.target.value })}
          />
          <Cell
            label="Status"
            value={row.status}
            onChange={(event) => controls.update(row.key, { status: event.target.value })}
          />
          <Cell
            label="Deliverable"
            value={row.deliverable}
            onChange={(event) => controls.update(row.key, { deliverable: event.target.value })}
          />
          <Cell
            label="Planned %"
            type="number"
            value={row.plannedPct}
            onChange={(event) => controls.update(row.key, { plannedPct: event.target.value })}
          />
          <Cell
            label="Actual %"
            type="number"
            value={row.actualPct}
            onChange={(event) => controls.update(row.key, { actualPct: event.target.value })}
          />
          <Cell
            label="Time planned"
            type="number"
            value={row.timePlanned}
            onChange={(event) => controls.update(row.key, { timePlanned: event.target.value })}
          />
          <Cell
            label="Time spent"
            type="number"
            value={row.timeSpent}
            onChange={(event) => controls.update(row.key, { timeSpent: event.target.value })}
          />
        </div>
      )}
    />
  );
}

export function BlockersFieldset({ controls }: { controls: FlaggedRowListControls<BlockerRow> }) {
  return (
    <DynamicList
      legend="Blockers"
      addLabel="Add blocker"
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <Cell
            label="Description"
            value={row.description}
            onChange={(event) => controls.update(row.key, { description: event.target.value })}
          />
          <label className="flex items-center gap-2 text-xs">
            <input
              type="checkbox"
              checked={row.isKeyIssue}
              onChange={(event) => controls.setExclusive(row.key, event.target.checked)}
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
      addLabel="Add achievement"
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <Cell
            label="Description"
            value={row.description}
            onChange={(event) => controls.update(row.key, { description: event.target.value })}
          />
          <label className="flex items-center gap-2 text-xs">
            <input
              type="checkbox"
              checked={row.isKeyHighlight}
              onChange={(event) => controls.setExclusive(row.key, event.target.checked)}
            />
            Key highlight
          </label>
        </div>
      )}
    />
  );
}

export function HoursFieldset({ controls }: { controls: RowListControls<HoursRow> }) {
  return (
    <DynamicList
      legend="Hours breakdown"
      addLabel="Add hours row"
      rows={controls.rows}
      onAdd={controls.add}
      onRemove={controls.remove}
      renderRow={(row) => (
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end">
          <Cell
            label="Task type"
            value={row.taskType}
            onChange={(event) => controls.update(row.key, { taskType: event.target.value })}
          />
          <Cell
            label="Hours"
            type="number"
            value={row.hours}
            onChange={(event) => controls.update(row.key, { hours: event.target.value })}
          />
        </div>
      )}
    />
  );
}
