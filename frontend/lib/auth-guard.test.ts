import type { AuthUser } from "./api-client";
import { evaluateAuthGuard } from "./auth-guard";

const MEMBER: AuthUser = { id: 1, name: "Lin", role: "MEMBER" };
const MANAGER: AuthUser = { id: 2, name: "Ada", role: "MANAGER" };
const ADMIN: AuthUser = { id: 3, name: "Grace", role: "ADMIN" };

describe("evaluateAuthGuard", () => {
  it("holds off while the session is still loading, without redirecting", () => {
    expect(evaluateAuthGuard({ isLoading: true, user: null, token: null })).toEqual({
      redirectTo: null,
      isChecking: true,
    });
  });

  it("holds off while loading even if a stale user/token happen to be present", () => {
    expect(evaluateAuthGuard({ isLoading: true, user: MANAGER, token: "jwt" })).toEqual({
      redirectTo: null,
      isChecking: true,
    });
  });

  it("redirects to /login once loading has finished with no user", () => {
    expect(evaluateAuthGuard({ isLoading: false, user: null, token: null })).toEqual({
      redirectTo: "/login",
      isChecking: true,
    });
  });

  it("redirects to /login when a user is present but the token is missing", () => {
    expect(evaluateAuthGuard({ isLoading: false, user: MANAGER, token: null })).toEqual({
      redirectTo: "/login",
      isChecking: true,
    });
  });

  it("allows rendering once loading has finished and no roles are required", () => {
    expect(evaluateAuthGuard({ isLoading: false, user: MANAGER, token: "jwt" })).toEqual({
      redirectTo: null,
      isChecking: false,
    });
  });

  it("allows rendering when the user's role is in the allowed list", () => {
    expect(
      evaluateAuthGuard({ isLoading: false, user: ADMIN, token: "jwt" }, ["ADMIN", "MANAGER"]),
    ).toEqual({ redirectTo: null, isChecking: false });
  });

  it("sends a MEMBER who hits a manager-only route to the member landing route", () => {
    expect(
      evaluateAuthGuard({ isLoading: false, user: MEMBER, token: "jwt" }, ["MANAGER"]),
    ).toEqual({ redirectTo: "/reports", isChecking: true });
  });

  it("sends a MANAGER who hits a member-only route to the manager landing route", () => {
    expect(
      evaluateAuthGuard({ isLoading: false, user: MANAGER, token: "jwt" }, ["MEMBER"]),
    ).toEqual({ redirectTo: "/projects", isChecking: true });
  });

  it("lets a MEMBER through a member-only route", () => {
    expect(
      evaluateAuthGuard({ isLoading: false, user: MEMBER, token: "jwt" }, ["MEMBER"]),
    ).toEqual({ redirectTo: null, isChecking: false });
  });

  it("lets either role through a route both can see", () => {
    expect(
      evaluateAuthGuard({ isLoading: false, user: MANAGER, token: "jwt" }, ["MEMBER", "MANAGER"]),
    ).toEqual({ redirectTo: null, isChecking: false });
  });
});
