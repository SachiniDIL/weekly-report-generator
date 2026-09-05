import { request, type Role } from "@/lib/api-client";

export type UserStatus = "PENDING" | "ACTIVE" | "REMOVED";

/** Mirrors the backend's AdminUserView. A PENDING signup has no role yet. */
export interface AdminUserView {
  id: number;
  name: string;
  email: string;
  role: Role | null;
  status: UserStatus;
  createdAt: string;
}

export interface AdminCreateUserRequest {
  name: string;
  email: string;
  password: string;
  role: Role;
}

export function listAdminUsers(status?: UserStatus): Promise<AdminUserView[]> {
  return request("/admin/users", { method: "GET", query: { status } });
}

export function createAdminUser(payload: AdminCreateUserRequest): Promise<AdminUserView> {
  return request("/admin/users", { method: "POST", body: payload });
}

export function approveUser(id: number, role: Role): Promise<AdminUserView> {
  return request(`/admin/users/${id}/approve`, { method: "POST", body: { role } });
}

export function changeUserRole(id: number, role: Role): Promise<AdminUserView> {
  return request(`/admin/users/${id}/role`, { method: "PATCH", body: { role } });
}

/** DELETE is hard for a PENDING user, soft (status -> REMOVED) for an ACTIVE one — the backend decides. */
export function removeUser(id: number): Promise<void> {
  return request(`/admin/users/${id}`, { method: "DELETE" });
}
