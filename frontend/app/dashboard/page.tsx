"use client";

import { useAuth } from "@/lib/auth-context";
import { useRequireAuth } from "@/lib/use-require-auth";

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const { isChecking } = useRequireAuth();

  if (isChecking) {
    return null;
  }

  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-4 p-4">
      <h1 className="text-xl font-semibold">
        {user ? `Welcome, ${user.name} (${user.role})` : "Welcome"}
      </h1>
      <button type="button" onClick={logout} className="text-sm underline">
        Sign out
      </button>
    </main>
  );
}
