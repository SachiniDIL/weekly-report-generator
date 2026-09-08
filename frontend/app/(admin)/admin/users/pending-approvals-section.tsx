"use client";

import { useState } from "react";
import type { Role } from "@/lib/api-client";
import type { AdminUserView } from "@/lib/api/admin-users";
import { Avatar } from "@/lib/avatar";
import { useConfirm } from "@/lib/confirm-dialog";
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
  const confirm = useConfirm();
  const busy = approve.isPending || reject.isPending;

  async function handleApprove() {
    const confirmed = await confirm({
      title: "Approve this signup?",
      message: `${user.name} (${user.email}) gets an active ${role} account and can sign in immediately.`,
      confirmLabel: "Approve",
    });
    if (confirmed) {
      approve.mutate({ id: user.id, role });
    }
  }

  async function handleReject() {
    const confirmed = await confirm({
      title: "Reject this signup?",
      message: `${user.name}'s pending account will be deleted. They'd need to register again.`,
      confirmLabel: "Reject",
      tone: "danger",
    });
    if (confirmed) {
      reject.mutate(user.id);
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
            onClick={handleApprove}
            className="rounded border border-black/20 px-3 py-1 disabled:opacity-40 dark:border-white/25"
          >
            Approve
          </button>
          <button
            type="button"
            disabled={busy}
            onClick={handleReject}
            className="text-red-600 underline disabled:opacity-40"
          >
            Reject
          </button>
        </div>
      </div>
    </li>
  );
}
