import { useQuery } from "@tanstack/react-query";
import { listProjects, type ProjectResponse } from "@/lib/api/projects";

export function useProjectsQuery(includeInactive: boolean) {
  return useQuery<ProjectResponse[]>({
    queryKey: ["projects", { includeInactive }],
    queryFn: () => listProjects(includeInactive),
  });
}
