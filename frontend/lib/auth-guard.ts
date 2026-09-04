import type { AuthUser, Role } from "@/lib/api-client";

const LOGIN_ROUTE = "/login";
// No role-specific pages exist yet, so a forbidden role always falls back here. Point this
// somewhere more specific once real role-gated pages exist.
const FORBIDDEN_ROLE_FALLBACK_ROUTE = "/dashboard";

export interface AuthGuardState {
  isLoading: boolean;
  user: AuthUser | null;
  token: string | null;
}

export interface AuthGuardDecision {
  /** Route to send the visitor to, or null if the page may render as-is. */
  redirectTo: string | null;
  /** True while the caller should hold off rendering protected content. */
  isChecking: boolean;
}

/**
 * Pure decision logic for the auth route guard, kept separate from useRequireAuth so it can be
 * unit tested without rendering a component or mocking the router.
 */
export function evaluateAuthGuard(
  state: AuthGuardState,
  allowedRoles?: Role[],
): AuthGuardDecision {
  if (state.isLoading) {
    return { redirectTo: null, isChecking: true };
  }

  if (!state.user || !state.token) {
    return { redirectTo: LOGIN_ROUTE, isChecking: true };
  }

  if (allowedRoles && !allowedRoles.includes(state.user.role)) {
    return { redirectTo: FORBIDDEN_ROLE_FALLBACK_ROUTE, isChecking: true };
  }

  return { redirectTo: null, isChecking: false };
}
