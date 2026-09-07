import { useQuery } from "@tanstack/react-query";
import { listAssignedProjects, type ProjectResponse } from "@/lib/api/projects";

/** The member's own assigned active projects, for the create-report project picker. */
export function useProjectOptionsQuery() {
  return useQuery<ProjectResponse[]>({
    queryKey: ["projects", "assigned"],
    queryFn: listAssignedProjects,
  });
}
