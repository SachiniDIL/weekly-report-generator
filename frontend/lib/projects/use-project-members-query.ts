import { useQuery } from "@tanstack/react-query";
import { listProjectMembers, type ProjectMemberView } from "@/lib/api/projects";

export function useProjectMembersQuery(projectId: number, enabled = true) {
  return useQuery<ProjectMemberView[]>({
    queryKey: ["project", projectId, "members"],
    queryFn: () => listProjectMembers(projectId),
    enabled,
  });
}
