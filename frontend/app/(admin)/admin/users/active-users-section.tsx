"use client";

import type { Role } from "@/lib/api-client";
import type { AdminUserView } from "@/lib/api/admin-users";
import { Avatar } from "@/lib/avatar";
import { useConfirm } from "@/lib/confirm-dialog";
import {
  useChangeUserRoleMutation,
  useRemoveUserMutation,
} from "./use-admin-user-mutations";

const ALL_ROLES: Role[] = ["ADMIN", "MANAGER", "MEMBER"];

export function ActiveUsersSection({ users }: { users: AdminUserView[] }) {
  const adminCount = users.filter((user) => user.role === "ADMIN").length;

  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-dusk-secondary">
        Active users
      </h2>
      {users.length === 0 ? (
        <p className="text-sm text-dusk-secondary">No active users.</p>
      ) : (
        <ul className="flex flex-col divide-y divide-black/10">
          {users.map((user) => (
            <ActiveUserRow
              key={user.id}
              user={user}
              isLastAdmin={user.role === "ADMIN" && adminCount <= 1}
            />
          ))}
        </ul>
      )}
    </section>
  );
}

function ActiveUserRow({
  user,
  isLastAdmin,
}: {
  user: AdminUserView;
  isLastAdmin: boolean;
}) {
  const changeRole = useChangeUserRoleMutation();
  const remove = useRemoveUserMutation();
  const confirm = useConfirm();
  const busy = changeRole.isPending || remove.isPending;
  const currentRole = user.role ?? "MEMBER";
  // Admin accounts are protected — the backend rejects removing one, so don't offer it here.
  const isAdmin = currentRole === "ADMIN";

  async function handleRoleChange(nextRole: Role) {
    if (nextRole === currentRole) {
      return;
    }
    const confirmed = await confirm({
      title: "Change this user's role?",
      message: `${user.name} will change from ${currentRole} to ${nextRole}. Their access changes immediately.`,
      confirmLabel: "Change role",
    });
    if (confirmed) {
      changeRole.mutate({ id: user.id, role: nextRole });
    }
  }

  async function handleRemove() {
    const confirmed = await confirm({
      title: "Remove this user?",
      message: `${user.name} (${user.email}) loses access immediately and any active sessions are ended. This can't be undone.`,
      confirmLabel: "Remove user",
      tone: "danger",
    });
    if (confirmed) {
      remove.mutate(user.id);
    }
  }

  return (
    <li className="dusk-row flex flex-col gap-1 px-2 py-2 text-sm">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <span className="flex items-center gap-2 text-dusk-primary">
          <Avatar name={user.name} size={20} />
          {user.name}{" "}
          <span className="text-dusk-secondary">({user.email})</span>
        </span>
        <div className="flex items-center gap-2">
          <select
            aria-label={`Role for ${user.name}`}
            value={currentRole}
            // The last admin can't be demoted — the system must keep one.
            disabled={busy || isLastAdmin}
            onChange={(event) => handleRoleChange(event.target.value as Role)}
            className="rounded border border-black/15 bg-transparent px-2 py-1 dark:border-white/20 disabled:opacity-60"
          >
            {ALL_ROLES.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          {isLastAdmin ? (
            <span className="text-xs text-dusk-muted">Only admin</span>
          ) : isAdmin ? (
            <span className="text-xs text-dusk-muted">Protected</span>
          ) : (
            <button
              type="button"
              disabled={busy}
              onClick={handleRemove}
              className="text-red-600 underline disabled:opacity-40"
            >
              Remove
            </button>
          )}
        </div>
      </div>
    </li>
  );
}
