"use client";

import { describeError } from "@/lib/api-client";
import type { Role } from "@/lib/api-client";
import type { AdminUserView } from "@/lib/api/admin-users";
import { useChangeUserRoleMutation, useRemoveUserMutation } from "./use-admin-user-mutations";

const ALL_ROLES: Role[] = ["ADMIN", "MANAGER", "MEMBER"];

export function ActiveUsersSection({ users }: { users: AdminUserView[] }) {
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">Active users</h2>
      {users.length === 0 ? (
        <p className="text-sm text-gray-500">No active users.</p>
      ) : (
        <ul className="flex flex-col divide-y divide-black/10 dark:divide-white/15">
          {users.map((user) => (
            <ActiveUserRow key={user.id} user={user} />
          ))}
        </ul>
      )}
    </section>
  );
}

function ActiveUserRow({ user }: { user: AdminUserView }) {
  const changeRole = useChangeUserRoleMutation();
  const remove = useRemoveUserMutation();
  const busy = changeRole.isPending || remove.isPending;
  const error = changeRole.error ?? remove.error;

  return (
    <li className="flex flex-col gap-1 py-2 text-sm">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <span>
          {user.name} <span className="text-gray-500">({user.email})</span>
        </span>
        <div className="flex items-center gap-2">
          <select
            aria-label={`Role for ${user.name}`}
            value={user.role ?? "MEMBER"}
            disabled={busy}
            onChange={(event) =>
              changeRole.mutate({ id: user.id, role: event.target.value as Role })
            }
            className="rounded border border-black/15 bg-transparent px-2 py-1 dark:border-white/20"
          >
            {ALL_ROLES.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          <button
            type="button"
            disabled={busy}
            onClick={() => remove.mutate(user.id)}
            className="text-red-600 underline disabled:opacity-40"
          >
            Remove
          </button>
        </div>
      </div>
      {error ? (
        <p role="alert" className="text-red-700 dark:text-red-300">
          {describeError(error)}
        </p>
      ) : null}
    </li>
  );
}
