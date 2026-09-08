/** Task-entry priority and status: the selectable options, plus display labels and tones. */

export const TASK_PRIORITY_OPTIONS = [
  { value: "HIGH", label: "High" },
  { value: "MEDIUM", label: "Medium" },
  { value: "LOW", label: "Low" },
] as const;

export const TASK_STATUS_OPTIONS = [
  { value: "NOT_STARTED", label: "Not started" },
  { value: "IN_PROGRESS", label: "In progress" },
  { value: "BLOCKED", label: "Blocked" },
  { value: "IN_REVIEW", label: "In review" },
  { value: "DONE", label: "Done" },
] as const;

export type ChipTone = "neutral" | "info" | "warning" | "danger" | "success";

const PRIORITY_TONE: Record<string, ChipTone> = {
  HIGH: "danger",
  MEDIUM: "warning",
  LOW: "neutral",
};

const STATUS_TONE: Record<string, ChipTone> = {
  NOT_STARTED: "neutral",
  IN_PROGRESS: "info",
  BLOCKED: "danger",
  IN_REVIEW: "warning",
  DONE: "success",
};

/** Sentence-case a stored enum value we don't have an explicit label for. */
function prettify(value: string): string {
  if (!value) {
    return "—";
  }
  return value.charAt(0) + value.slice(1).toLowerCase().replace(/_/g, " ");
}

function labelFrom(
  options: ReadonlyArray<{ value: string; label: string }>,
  value: string,
): string {
  return (
    options.find((option) => option.value === value)?.label ?? prettify(value)
  );
}

export const priorityLabel = (value: string): string =>
  labelFrom(TASK_PRIORITY_OPTIONS, value);
export const statusLabel = (value: string): string =>
  labelFrom(TASK_STATUS_OPTIONS, value);
export const priorityTone = (value: string): ChipTone =>
  PRIORITY_TONE[value] ?? "neutral";
export const statusTone = (value: string): ChipTone =>
  STATUS_TONE[value] ?? "neutral";
