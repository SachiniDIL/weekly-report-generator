"use client";

import Link from "next/link";
import { describeError } from "@/lib/api-client";
import { Avatar } from "@/lib/avatar";
import { SkeletonText } from "@/lib/skeleton";
import { useTeamMembersQuery } from "@/lib/team/use-team-members-query";

export default function TeamPage() {
  const members = useTeamMembersQuery();

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-6">
      <h1 className="text-xl font-semibold text-dusk-primary">Team</h1>

      {members.isPending ? <SkeletonText lines={5} /> : null}
      {members.isError ? (
        <p role="alert" className="text-sm text-red-700">
          {describeError(members.error)}
        </p>
      ) : null}

      {members.isSuccess && members.data.length === 0 ? (
        <p className="text-sm text-dusk-secondary">No team members yet.</p>
      ) : null}

      {members.isSuccess && members.data.length > 0 ? (
        <ul className="dusk-panel divide-y divide-black/10 p-2">
          {members.data.map((member) => (
            <li key={member.id}>
              <Link
                href={`/team/${member.id}`}
                className="dusk-row flex items-center gap-3 px-2 py-2.5 text-sm"
              >
                <Avatar name={member.name} size={28} />
                <span className="font-medium text-dusk-primary">
                  {member.name}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      ) : null}
    </main>
  );
}
