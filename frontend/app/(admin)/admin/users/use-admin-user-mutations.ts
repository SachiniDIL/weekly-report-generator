"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { Role } from "@/lib/api-client";
import {
  approveUser,
  changeUserRole,
  createAdminUser,
  removeUser,
  type AdminCreateUserRequest,
  type AdminUserView,
} from "@/lib/api/admin-users";

/** Every admin-user action invalidates the same list, so share that wiring. */
function useAdminUserMutation<TData, TVariables>(
  mutationFn: (variables: TVariables) => Promise<TData>,
) {
  const queryClient = useQueryClient();
  return useMutation<TData, Error, TVariables>({
    mutationFn,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-users"] }),
  });
}

export function useApproveUserMutation() {
  return useAdminUserMutation<AdminUserView, { id: number; role: Role }>(({ id, role }) =>
    approveUser(id, role),
  );
}

export function useChangeUserRoleMutation() {
  return useAdminUserMutation<AdminUserView, { id: number; role: Role }>(({ id, role }) =>
    changeUserRole(id, role),
  );
}

export function useRemoveUserMutation() {
  return useAdminUserMutation<void, number>((id) => removeUser(id));
}

export function useCreateAdminUserMutation() {
  return useAdminUserMutation<AdminUserView, AdminCreateUserRequest>(createAdminUser);
}
