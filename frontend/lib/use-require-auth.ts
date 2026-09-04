"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import type { Role } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";
import { evaluateAuthGuard } from "@/lib/auth-guard";

export interface UseRequireAuthResult {
  /**
   * True while the session is still rehydrating or a redirect has just been triggered — the
   * caller should not render protected content while this is true.
   */
  isChecking: boolean;
}

/**
 * Client-side route guard for pages that require an authenticated session. Waits out
 * AuthContext's isLoading before deciding, so it never redirects mid-rehydration; once resolved,
 * it sends unauthenticated visitors to /login and, if allowedRoles is given, sends visitors
 * whose role isn't in that list to a fallback route.
 */
export function useRequireAuth(allowedRoles?: Role[]): UseRequireAuthResult {
  const { user, token, isLoading } = useAuth();
  const router = useRouter();

  const decision = evaluateAuthGuard({ isLoading, user, token }, allowedRoles);

  useEffect(() => {
    if (decision.redirectTo) {
      router.replace(decision.redirectTo);
    }
  }, [decision.redirectTo, router]);

  return { isChecking: decision.isChecking };
}
