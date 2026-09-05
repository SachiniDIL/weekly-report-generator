"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  assignProjectMember,
  unassignProjectMember,
  type ProjectMemberView,
} from "@/lib/api/projects";

export function useAssignMemberMutation(projectId: number) {
  const queryClient = useQueryClient();

  return useMutation<ProjectMemberView, Error, number>({
    mutationFn: (userId) => assignProjectMember(projectId, userId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["project", projectId, "members"] }),
  });
}

export function useUnassignMemberMutation(projectId: number) {
  const queryClient = useQueryClient();

  return useMutation<void, Error, number>({
    mutationFn: (userId) => unassignProjectMember(projectId, userId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["project", projectId, "members"] }),
  });
}
