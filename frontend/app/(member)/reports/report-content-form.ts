import type {
  ReportContentRequest,
  ReportContentResponse,
} from "@/lib/api/reports";

// Numeric inputs are held as strings while editing (empty stays empty, no NaN) and parsed on save.
export interface TaskEntryRow {
  key: string;
  taskName: string;
  priority: string;
  plannedPct: string;
  actualPct: string;
  status: string;
  timePlanned: string;
  timeSpent: string;
  deliverable: string;
}

export interface BlockerRow {
  key: string;
  description: string;
  isKeyIssue: boolean;
}

export interface AchievementRow {
  key: string;
  description: string;
  isKeyHighlight: boolean;
}

export interface HoursRow {
  key: string;
  taskType: string;
  hours: string;
}

export interface ReportContentForm {
  tasksPlannedNext: string;
  notes: string;
  links: string;
  taskEntries: TaskEntryRow[];
  blockers: BlockerRow[];
  achievements: AchievementRow[];
  hours: HoursRow[];
}

let rowSequence = 0;

export function nextRowKey(): string {
  rowSequence += 1;
  return `row-${rowSequence}`;
}

export function emptyReportContentForm(): ReportContentForm {
  return {
    tasksPlannedNext: "",
    notes: "",
    links: "",
    taskEntries: [],
    blockers: [],
    achievements: [],
    hours: [],
  };
}

export function emptyTaskEntryRow(): TaskEntryRow {
  return {
    key: nextRowKey(),
    taskName: "",
    priority: "",
    plannedPct: "",
    actualPct: "",
    status: "",
    timePlanned: "",
    timeSpent: "",
    deliverable: "",
  };
}

export function emptyBlockerRow(): BlockerRow {
  return { key: nextRowKey(), description: "", isKeyIssue: false };
}

export function emptyAchievementRow(): AchievementRow {
  return { key: nextRowKey(), description: "", isKeyHighlight: false };
}

export function emptyHoursRow(): HoursRow {
  return { key: nextRowKey(), taskType: "", hours: "" };
}

export function reportContentFormFromResponse(content: ReportContentResponse): ReportContentForm {
  return {
    tasksPlannedNext: content.tasksPlannedNext ?? "",
    notes: content.notes ?? "",
    links: content.links ?? "",
    taskEntries: content.taskEntries.map((entry) => ({
      key: nextRowKey(),
      taskName: entry.taskName,
      priority: entry.priority,
      plannedPct: String(entry.plannedPct),
      actualPct: String(entry.actualPct),
      status: entry.status,
      timePlanned: entry.timePlanned == null ? "" : String(entry.timePlanned),
      timeSpent: entry.timeSpent == null ? "" : String(entry.timeSpent),
      deliverable: entry.deliverable ?? "",
    })),
    blockers: content.blockers.map((blocker) => ({
      key: nextRowKey(),
      description: blocker.description,
      isKeyIssue: blocker.isKeyIssue,
    })),
    achievements: content.achievements.map((achievement) => ({
      key: nextRowKey(),
      description: achievement.description,
      isKeyHighlight: achievement.isKeyHighlight,
    })),
    hours: content.hoursBreakdown.map((row) => ({
      key: nextRowKey(),
      taskType: row.taskType,
      hours: String(row.hours),
    })),
  };
}

export function toReportContentRequest(form: ReportContentForm): ReportContentRequest {
  return {
    tasksPlannedNext: emptyToNull(form.tasksPlannedNext),
    notes: emptyToNull(form.notes),
    links: emptyToNull(form.links),
    taskEntries: form.taskEntries.map((row) => ({
      taskName: row.taskName.trim(),
      priority: row.priority.trim(),
      plannedPct: toIntOrZero(row.plannedPct),
      actualPct: toIntOrZero(row.actualPct),
      status: row.status.trim(),
      timePlanned: toOptionalInt(row.timePlanned),
      timeSpent: toOptionalInt(row.timeSpent),
      deliverable: emptyToNull(row.deliverable),
    })),
    blockers: form.blockers.map((row) => ({
      description: row.description.trim(),
      isKeyIssue: row.isKeyIssue,
    })),
    achievements: form.achievements.map((row) => ({
      description: row.description.trim(),
      isKeyHighlight: row.isKeyHighlight,
    })),
    hoursBreakdown: form.hours.map((row) => ({
      taskType: row.taskType.trim(),
      hours: Number(row.hours),
    })),
  };
}

/**
 * Checking one row's key flag clears it on every other row — the "exactly one key
 * blocker / highlight" rule the backend's partial unique index also enforces. Unchecking is
 * unconditional.
 */
export function setExclusiveFlag<Row extends { key: string }>(
  rows: Row[],
  targetKey: string,
  flag: keyof Row,
  checked: boolean,
): Row[] {
  return rows.map((row) => {
    if (row.key === targetKey) {
      return { ...row, [flag]: checked };
    }
    return checked ? { ...row, [flag]: false } : row;
  });
}

export interface ReportIdentityDraft {
  projectId: number | null;
  weekStart: string;
  weekEnd: string;
}

/** Human-readable problems; an empty array means the form is safe to send. */
export function validateReportContentForm(
  form: ReportContentForm,
  identity?: ReportIdentityDraft,
): string[] {
  const problems: string[] = [];

  if (identity) {
    if (identity.projectId == null) {
      problems.push("Choose a project.");
    }
    if (!identity.weekStart) {
      problems.push("Set the week start date.");
    }
    if (!identity.weekEnd) {
      problems.push("Set the week end date.");
    }
    if (identity.weekStart && identity.weekEnd && identity.weekEnd < identity.weekStart) {
      problems.push("The week end date can't be before the week start date.");
    }
  }

  form.taskEntries.forEach((row, index) => {
    if (!row.taskName.trim() || !row.priority.trim() || !row.status.trim()) {
      problems.push(`Task ${index + 1} needs a name, priority, and status.`);
    }
  });

  form.blockers.forEach((row, index) => {
    if (!row.description.trim()) {
      problems.push(`Blocker ${index + 1} needs a description.`);
    }
  });

  form.achievements.forEach((row, index) => {
    if (!row.description.trim()) {
      problems.push(`Achievement ${index + 1} needs a description.`);
    }
  });

  form.hours.forEach((row, index) => {
    if (!row.taskType.trim() || row.hours.trim() === "" || Number.isNaN(Number(row.hours))) {
      problems.push(`Hours row ${index + 1} needs a task type and a numeric hours value.`);
    }
  });

  return problems;
}

function emptyToNull(value: string): string | null {
  const trimmed = value.trim();
  return trimmed === "" ? null : trimmed;
}

function toIntOrZero(value: string): number {
  const parsed = Number.parseInt(value, 10);
  return Number.isNaN(parsed) ? 0 : parsed;
}

function toOptionalInt(value: string): number | null {
  if (value.trim() === "") {
    return null;
  }
  const parsed = Number.parseInt(value, 10);
  return Number.isNaN(parsed) ? null : parsed;
}
