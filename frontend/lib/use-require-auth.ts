"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import type { Role } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";
import { evaluateAuthGuard } from "@/lib/auth-guard";

export interface UseRequireAuthOptions {
  /** If given, a signed-in user whose role isn't listed is redirected to their own landing route. */
  allowedRoles?: Role[];
}

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
 * whose role isn't in that list to their own landing route.
 */
export function useRequireAuth(options: UseRequireAuthOptions = {}): UseRequireAuthResult {
  const { user, token, isLoading } = useAuth();
  const router = useRouter();

  const decision = evaluateAuthGuard({ isLoading, user, token }, options.allowedRoles);

  useEffect(() => {
    if (decision.redirectTo) {
      router.replace(decision.redirectTo);
    }
  }, [decision.redirectTo, router]);

  return { isChecking: decision.isChecking };
}
