"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { Avatar } from "@/lib/avatar";
import { useAssignableUsersQuery } from "@/lib/projects/use-assignable-users-query";
import { useProjectMembersQuery } from "@/lib/projects/use-project-members-query";
import {
  useAssignMemberMutation,
  useUnassignMemberMutation,
} from "./use-member-mutations";

export function ProjectMembersPanel({ projectId }: { projectId: number }) {
  const members = useProjectMembersQuery(projectId);
  const assignable = useAssignableUsersQuery();
  const assign = useAssignMemberMutation(projectId);
  const unassign = useUnassignMemberMutation(projectId);
  const [selectedUserId, setSelectedUserId] = useState("");

  const memberIds = new Set(
    (members.data ?? []).map((member) => member.userId),
  );
  const options = (assignable.data ?? []).filter(
    (user) => !memberIds.has(user.id),
  );

  function handleAssign(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (selectedUserId === "") {
      return;
    }
    assign.mutate(Number(selectedUserId), {
      onSuccess: () => setSelectedUserId(""),
    });
  }

  return (
    <div className="mt-3 flex flex-col gap-3 border-t border-black/10 pt-3 dark:border-white/15">
      <h4 className="text-xs font-semibold uppercase tracking-wide text-gray-500">
        Members
      </h4>

      {members.isPending ? (
        <p className="text-sm text-gray-500">Loading members…</p>
      ) : members.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(members.error)}
        </p>
      ) : members.data.length === 0 ? (
        <p className="text-sm text-gray-500">No members assigned.</p>
      ) : (
        <ul className="flex flex-col gap-1 text-sm">
          {members.data.map((member) => (
            <li
              key={member.userId}
              className="dusk-row flex items-center justify-between gap-2 px-2 py-1"
            >
              <span className="flex items-center gap-2 text-dusk-primary">
                <Avatar name={member.name} size={20} />
                {member.name}{" "}
                <span className="text-dusk-secondary">({member.email})</span>
              </span>
              <button
                type="button"
                onClick={() => unassign.mutate(member.userId)}
                disabled={unassign.isPending}
                className="text-xs text-red-600 underline disabled:opacity-50"
              >
                Remove
              </button>
            </li>
          ))}
        </ul>
      )}

      <form
        onSubmit={handleAssign}
        className="flex flex-wrap items-center gap-2"
      >
        <select
          aria-label="Assign a member"
          value={selectedUserId}
          onChange={(event) => setSelectedUserId(event.target.value)}
          className="rounded border border-black/15 bg-transparent px-2 py-1 text-sm dark:border-white/20"
        >
          <option value="">Select a user…</option>
          {options.map((user) => (
            <option key={user.id} value={user.id}>
              {user.name} — {user.role}
            </option>
          ))}
        </select>
        <button
          type="submit"
          disabled={selectedUserId === "" || assign.isPending}
          className="rounded-lg border border-white/20 px-3 py-1 text-sm text-dusk-primary transition-colors hover:border-dusk-accent disabled:opacity-40"
        >
          Add
        </button>
      </form>

      {assign.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(assign.error)}
        </p>
      ) : null}
      {unassign.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(unassign.error)}
        </p>
      ) : null}
    </div>
  );
}
