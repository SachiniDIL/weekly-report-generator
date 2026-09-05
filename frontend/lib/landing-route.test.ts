import { landingRouteForRole } from "./landing-route";

describe("landingRouteForRole", () => {
  it("sends a MEMBER to their report history", () => {
    expect(landingRouteForRole("MEMBER")).toBe("/reports");
  });

  it("sends a MANAGER to the projects page", () => {
    expect(landingRouteForRole("MANAGER")).toBe("/projects");
  });

  it("sends an ADMIN to the user management page", () => {
    expect(landingRouteForRole("ADMIN")).toBe("/admin/users");
  });
});
