import { currentIsoWeek } from "./current-iso-week";

describe("currentIsoWeek", () => {
  it("returns the Monday–Sunday range for a mid-week date", () => {
    expect(currentIsoWeek(new Date(2026, 8, 2))).toEqual({
      weekStart: "2026-08-31",
      weekEnd: "2026-09-06",
    });
  });

  it("keeps a Monday as the start of its own week", () => {
    expect(currentIsoWeek(new Date(2026, 7, 31))).toEqual({
      weekStart: "2026-08-31",
      weekEnd: "2026-09-06",
    });
  });

  it("treats Sunday as the end of the week that began the prior Monday", () => {
    expect(currentIsoWeek(new Date(2026, 8, 6))).toEqual({
      weekStart: "2026-08-31",
      weekEnd: "2026-09-06",
    });
  });
});
