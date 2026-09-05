import { request, type Role } from "@/lib/api-client";

/** Mirrors the backend's UserBasicView — id, name, role only. */
export interface UserBasicView {
  id: number;
  name: string;
  role: Role;
}

/** Active users, Manager/Admin only. Optionally filtered to a single role. */
export function listUsers(role?: Role): Promise<UserBasicView[]> {
  return request("/users", { method: "GET", query: { role } });
}
