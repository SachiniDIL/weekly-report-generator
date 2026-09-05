import { landingRouteForRole } from "./landing-route";

describe("landingRouteForRole", () => {
  it("sends a MEMBER to their report history", () => {
    expect(landingRouteForRole("MEMBER")).toBe("/reports");
  });

  it("sends a MANAGER to the projects page", () => {
    expect(landingRouteForRole("MANAGER")).toBe("/projects");
  });

  it("keeps ADMIN on the dashboard placeholder for now", () => {
    expect(landingRouteForRole("ADMIN")).toBe("/dashboard");
  });
});
