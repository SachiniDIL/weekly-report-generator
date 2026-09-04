"use client";

import { useAuth } from "@/lib/auth-context";

export default function DashboardPage() {
  const { user, logout } = useAuth();

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
