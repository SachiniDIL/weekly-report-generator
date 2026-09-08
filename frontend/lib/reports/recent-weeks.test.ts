import { recentWeeks } from "./recent-weeks";

describe("recentWeeks", () => {
  it("returns the current week and the ones before it, newest first", () => {
    // Wednesday 2026-09-02 → its ISO week is 2026-08-31 … 2026-09-06.
    const weeks = recentWeeks(3, new Date(2026, 8, 2));

    expect(weeks).toEqual([
      { weekStart: "2026-08-31", weekEnd: "2026-09-06", label: "This week" },
      { weekStart: "2026-08-24", weekEnd: "2026-08-30", label: "Last week" },
      { weekStart: "2026-08-17", weekEnd: "2026-08-23", label: "2 weeks ago" },
    ]);
  });

  it("never includes a week that starts after the reference date", () => {
    const weeks = recentWeeks(4, new Date(2026, 8, 2));
    for (const week of weeks) {
      expect(week.weekStart <= "2026-09-02").toBe(true);
    }
  });
});
