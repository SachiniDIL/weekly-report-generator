"use client";

import { describeError } from "@/lib/api-client";
import { SkeletonText } from "@/lib/skeleton";
import { useAdminUsersQuery } from "@/lib/admin/use-admin-users-query";
import { ActiveUsersSection } from "./active-users-section";
import { CreateUserForm } from "./create-user-form";
import { PendingApprovalsSection } from "./pending-approvals-section";

export default function AdminUsersPage() {
  const users = useAdminUsersQuery();

  return (
    <main className="mx-auto flex max-w-3xl flex-col gap-8 p-6">
      <h1 className="text-xl font-semibold text-dusk-primary">
        User management
      </h1>

      {users.isPending ? <SkeletonText lines={5} /> : null}
      {users.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(users.error)}
        </p>
      ) : null}

      {users.isSuccess ? (
        <>
          <PendingApprovalsSection
            users={users.data.filter((user) => user.status === "PENDING")}
          />
          <ActiveUsersSection
            users={users.data.filter((user) => user.status === "ACTIVE")}
          />
          <CreateUserForm />
        </>
      ) : null}
    </main>
  );
}
