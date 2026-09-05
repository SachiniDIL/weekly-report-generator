"use client";

import { useState } from "react";
import {
  emptyAchievementRow,
  emptyBlockerRow,
  emptyHoursRow,
  emptyTaskEntryRow,
  setExclusiveFlag,
  type AchievementRow,
  type BlockerRow,
  type HoursRow,
  type ReportContentForm,
  type TaskEntryRow,
} from "./report-content-form";

export interface RowListControls<Row> {
  rows: Row[];
  add: () => void;
  remove: (key: string) => void;
  update: (key: string, patch: Partial<Row>) => void;
}

/** Adds mutual-exclusivity on top of the generic row list: checking one clears the others. */
export interface FlaggedRowListControls<Row> extends RowListControls<Row> {
  setExclusive: (key: string, checked: boolean) => void;
}

export interface ReportContentFormControls {
  form: ReportContentForm;
  setText: (field: "tasksPlannedNext" | "notes" | "links", value: string) => void;
  taskEntries: RowListControls<TaskEntryRow>;
  blockers: FlaggedRowListControls<BlockerRow>;
  achievements: FlaggedRowListControls<AchievementRow>;
  hours: RowListControls<HoursRow>;
}

type ListUpdater<Row> = (updater: (rows: Row[]) => Row[]) => void;

export function useReportContentForm(initial: ReportContentForm): ReportContentFormControls {
  const [form, setForm] = useState<ReportContentForm>(initial);

  const setText: ReportContentFormControls["setText"] = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const setTaskEntries: ListUpdater<TaskEntryRow> = (updater) =>
    setForm((current) => ({ ...current, taskEntries: updater(current.taskEntries) }));
  const setBlockers: ListUpdater<BlockerRow> = (updater) =>
    setForm((current) => ({ ...current, blockers: updater(current.blockers) }));
  const setAchievements: ListUpdater<AchievementRow> = (updater) =>
    setForm((current) => ({ ...current, achievements: updater(current.achievements) }));
  const setHours: ListUpdater<HoursRow> = (updater) =>
    setForm((current) => ({ ...current, hours: updater(current.hours) }));

  return {
    form,
    setText,
    taskEntries: buildRowListControls(form.taskEntries, setTaskEntries, emptyTaskEntryRow),
    blockers: buildFlaggedRowListControls(
      form.blockers,
      setBlockers,
      emptyBlockerRow,
      "isKeyIssue",
    ),
    achievements: buildFlaggedRowListControls(
      form.achievements,
      setAchievements,
      emptyAchievementRow,
      "isKeyHighlight",
    ),
    hours: buildRowListControls(form.hours, setHours, emptyHoursRow),
  };
}

function buildRowListControls<Row extends { key: string }>(
  rows: Row[],
  setRows: ListUpdater<Row>,
  makeRow: () => Row,
): RowListControls<Row> {
  return {
    rows,
    add: () => setRows((current) => [...current, makeRow()]),
    remove: (key) => setRows((current) => current.filter((row) => row.key !== key)),
    update: (key, patch) =>
      setRows((current) => current.map((row) => (row.key === key ? { ...row, ...patch } : row))),
  };
}

function buildFlaggedRowListControls<Row extends { key: string }>(
  rows: Row[],
  setRows: ListUpdater<Row>,
  makeRow: () => Row,
  flag: keyof Row,
): FlaggedRowListControls<Row> {
  return {
    ...buildRowListControls(rows, setRows, makeRow),
    setExclusive: (key, checked) =>
      setRows((current) => setExclusiveFlag(current, key, flag, checked)),
  };
}
