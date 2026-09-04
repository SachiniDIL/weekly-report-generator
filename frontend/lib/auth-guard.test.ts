import type { AuthUser } from "./api-client";
import { evaluateAuthGuard } from "./auth-guard";

const MANAGER: AuthUser = { id: 1, name: "Ada", role: "MANAGER" };
const ADMIN: AuthUser = { id: 2, name: "Grace", role: "ADMIN" };

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

  it("redirects to the fallback route when the user's role is not allowed", () => {
    expect(
      evaluateAuthGuard({ isLoading: false, user: MANAGER, token: "jwt" }, ["ADMIN"]),
    ).toEqual({ redirectTo: "/dashboard", isChecking: true });
  });
});
