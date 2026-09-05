import { request } from "@/lib/api-client";

export interface ProjectResponse {
  id: number;
  name: string;
  description: string | null;
  active: boolean;
}

export interface ProjectRequest {
  name: string;
  description?: string | null;
}

/** A project's assigned member — mirrors the backend's ProjectMemberView. */
export interface ProjectMemberView {
  userId: number;
  name: string;
  email: string;
}

export function listProjects(includeInactive = false): Promise<ProjectResponse[]> {
  return request("/projects", { method: "GET", query: { includeInactive } });
}

export function createProject(payload: ProjectRequest): Promise<ProjectResponse> {
  return request("/projects", { method: "POST", body: payload });
}

export function updateProject(id: number, payload: ProjectRequest): Promise<ProjectResponse> {
  return request(`/projects/${id}`, { method: "PUT", body: payload });
}

/** Archives the project (soft delete) — the backend never hard-deletes a project row. */
export function archiveProject(id: number): Promise<void> {
  return request(`/projects/${id}`, { method: "DELETE" });
}

export function listProjectMembers(projectId: number): Promise<ProjectMemberView[]> {
  return request(`/projects/${projectId}/members`, { method: "GET" });
}

export function assignProjectMember(projectId: number, userId: number): Promise<ProjectMemberView> {
  return request(`/projects/${projectId}/members/${userId}`, { method: "POST" });
}

export function unassignProjectMember(projectId: number, userId: number): Promise<void> {
  return request(`/projects/${projectId}/members/${userId}`, { method: "DELETE" });
}
