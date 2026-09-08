"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { describeError, type Role } from "@/lib/api-client";
import {
  approveUser,
  changeUserRole,
  createAdminUser,
  removeUser,
  type AdminCreateUserRequest,
  type AdminUserView,
} from "@/lib/api/admin-users";
import { useToast } from "@/lib/toast/toast-context";

/**
 * Every admin-user action invalidates the same list and reports its outcome with a toast — the
 * affected row often moves between the pending/active lists or disappears on success, so an
 * inline message next to the button is unreliable.
 */
function useAdminUserMutation<TData, TVariables>(
  mutationFn: (variables: TVariables) => Promise<TData>,
  successMessage: string | ((data: TData) => string),
) {
  const queryClient = useQueryClient();
  const toast = useToast();
  return useMutation<TData, Error, TVariables>({
    mutationFn,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["admin-users"] });
      toast.success(
        typeof successMessage === "function"
          ? successMessage(data)
          : successMessage,
      );
    },
    onError: (error) => toast.error(describeError(error)),
  });
}

export function useApproveUserMutation() {
  return useAdminUserMutation<AdminUserView, { id: number; role: Role }>(
    ({ id, role }) => approveUser(id, role),
    (user) => `${user.name} approved as ${user.role}.`,
  );
}

export function useChangeUserRoleMutation() {
  return useAdminUserMutation<AdminUserView, { id: number; role: Role }>(
    ({ id, role }) => changeUserRole(id, role),
    (user) => `${user.name}'s role changed to ${user.role}.`,
  );
}

export function useRemoveUserMutation() {
  return useAdminUserMutation<void, number>(
    (id) => removeUser(id),
    "User removed.",
  );
}

export function useCreateAdminUserMutation() {
  return useAdminUserMutation<AdminUserView, AdminCreateUserRequest>(
    createAdminUser,
    (user) => `${user.name} created.`,
  );
}
