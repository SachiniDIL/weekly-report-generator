import type { Role } from "@/lib/api-client";

const LANDING_ROUTE_BY_ROLE: Record<Role, string> = {
  MEMBER: "/reports",
  MANAGER: "/projects",
  ADMIN: "/admin/users",
};

/**
 * Where an authenticated user belongs when they have no more specific destination — used both
 * for the post-login redirect and as the route guard's fallback when a role can't see a route.
 */
export function landingRouteForRole(role: Role): string {
  return LANDING_ROUTE_BY_ROLE[role];
}
