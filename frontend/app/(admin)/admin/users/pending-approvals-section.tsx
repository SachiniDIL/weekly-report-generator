"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import type { Role } from "@/lib/api-client";
import type { AdminUserView } from "@/lib/api/admin-users";
import { Avatar } from "@/lib/avatar";
import {
  useApproveUserMutation,
  useRemoveUserMutation,
} from "./use-admin-user-mutations";

const APPROVAL_ROLES: Role[] = ["MEMBER", "MANAGER"];

export function PendingApprovalsSection({ users }: { users: AdminUserView[] }) {
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-dusk-secondary">
        Pending approvals
      </h2>
      {users.length === 0 ? (
        <p className="text-sm text-dusk-secondary">No pending signups.</p>
      ) : (
        <ul className="flex flex-col divide-y divide-black/10">
          {users.map((user) => (
            <PendingUserRow key={user.id} user={user} />
          ))}
        </ul>
      )}
    </section>
  );
}

function PendingUserRow({ user }: { user: AdminUserView }) {
  const [role, setRole] = useState<Role>("MEMBER");
  const approve = useApproveUserMutation();
  const reject = useRemoveUserMutation();
  const busy = approve.isPending || reject.isPending;
  const error = approve.error ?? reject.error;

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
            value={role}
            onChange={(event) => setRole(event.target.value as Role)}
            className="rounded border border-black/15 bg-transparent px-2 py-1 dark:border-white/20"
          >
            {APPROVAL_ROLES.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
          <button
            type="button"
            disabled={busy}
            onClick={() => approve.mutate({ id: user.id, role })}
            className="rounded border border-black/20 px-3 py-1 disabled:opacity-40 dark:border-white/25"
          >
            Approve
          </button>
          <button
            type="button"
            disabled={busy}
            onClick={() => reject.mutate(user.id)}
            className="text-red-600 underline disabled:opacity-40"
          >
            Reject
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
