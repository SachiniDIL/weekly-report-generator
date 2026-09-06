export interface IsoWeek {
  weekStart: string;
  weekEnd: string;
}

/**
 * The ISO week (Monday–Sunday) containing `reference`, as two `YYYY-MM-DD` strings.
 * Mirrors the backend default so the view opens on the same week the API would pick.
 */
export function currentIsoWeek(reference: Date = new Date()): IsoWeek {
  const monday = new Date(reference.getFullYear(), reference.getMonth(), reference.getDate());
  const mondayOffset = (monday.getDay() + 6) % 7;
  monday.setDate(monday.getDate() - mondayOffset);

  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);

  return { weekStart: toIsoDate(monday), weekEnd: toIsoDate(sunday) };
}

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
