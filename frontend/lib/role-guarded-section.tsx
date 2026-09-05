"use client";

import type { ReactNode } from "react";
import type { Role } from "@/lib/api-client";
import { useRequireAuth } from "@/lib/use-require-auth";

/**
 * Wraps a route subtree in a role check: unauthenticated or wrong-role visitors are redirected
 * away, and nothing renders until the guard has settled. Used by the (member) and (manager)
 * route-group layouts so the guard wiring lives in one place.
 */
export function RoleGuardedSection({
  allowedRoles,
  children,
}: {
  allowedRoles: Role[];
  children: ReactNode;
}) {
  const { isChecking } = useRequireAuth({ allowedRoles });

  if (isChecking) {
    return null;
  }

  return <>{children}</>;
}
