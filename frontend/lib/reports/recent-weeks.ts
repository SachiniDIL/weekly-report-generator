import { currentIsoWeek, type IsoWeek } from "@/lib/dashboard/current-iso-week";

export interface RecentWeek extends IsoWeek {
  /** "This week", "Last week", "2 weeks ago", … */
  label: string;
}

/**
 * The current ISO week plus `count - 1` weeks before it, newest first — the weeks a member is
 * allowed to file a report for. Never includes an upcoming week.
 */
export function recentWeeks(
  count: number,
  reference: Date = new Date(),
): RecentWeek[] {
  const weeks: RecentWeek[] = [];
  for (let offset = 0; offset < count; offset += 1) {
    const shifted = new Date(reference);
    shifted.setDate(shifted.getDate() - offset * 7);
    const { weekStart, weekEnd } = currentIsoWeek(shifted);
    weeks.push({ weekStart, weekEnd, label: labelFor(offset) });
  }
  return weeks;
}

function labelFor(offset: number): string {
  if (offset === 0) {
    return "This week";
  }
  if (offset === 1) {
    return "Last week";
  }
  return `${offset} weeks ago`;
}
