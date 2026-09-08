"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  archiveProject,
  createProject,
  updateProject,
  type ProjectRequest,
  type ProjectResponse,
} from "@/lib/api/projects";
import { useToast } from "@/lib/toast/toast-context";

/** Create the project when `projectId` is undefined, otherwise update it. */
export function useSaveProjectMutation(projectId: number | undefined) {
  const queryClient = useQueryClient();
  const toast = useToast();

  return useMutation<ProjectResponse, Error, ProjectRequest>({
    mutationFn: (payload) =>
      projectId == null
        ? createProject(payload)
        : updateProject(projectId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      toast.success(
        projectId == null ? "Project created." : "Project updated.",
      );
    },
  });
}

export function useArchiveProjectMutation() {
  const queryClient = useQueryClient();
  const toast = useToast();

  return useMutation<void, Error, number>({
    mutationFn: (projectId) => archiveProject(projectId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      toast.success("Project archived.");
    },
  });
}
