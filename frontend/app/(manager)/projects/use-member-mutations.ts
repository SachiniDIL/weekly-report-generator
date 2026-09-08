"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  assignProjectMember,
  unassignProjectMember,
  type ProjectMemberView,
} from "@/lib/api/projects";
import { useToast } from "@/lib/toast/toast-context";

export function useAssignMemberMutation(projectId: number) {
  const queryClient = useQueryClient();
  const toast = useToast();

  return useMutation<ProjectMemberView, Error, number>({
    mutationFn: (userId) => assignProjectMember(projectId, userId),
    onSuccess: (member) => {
      queryClient.invalidateQueries({
        queryKey: ["project", projectId, "members"],
      });
      toast.success(`${member.name} added to the project.`);
    },
  });
}

export function useUnassignMemberMutation(projectId: number) {
  const queryClient = useQueryClient();
  const toast = useToast();

  return useMutation<void, Error, number>({
    mutationFn: (userId) => unassignProjectMember(projectId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["project", projectId, "members"],
      });
      toast.success("Member removed from the project.");
    },
  });
}
