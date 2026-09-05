import { useQuery } from "@tanstack/react-query";
import { listProjects, type ProjectResponse } from "@/lib/api/projects";

/** Active projects for the create-report project picker. */
export function useProjectOptionsQuery() {
  return useQuery<ProjectResponse[]>({
    queryKey: ["projects", { includeInactive: false }],
    queryFn: () => listProjects(false),
  });
}
