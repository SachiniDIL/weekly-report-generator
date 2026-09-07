import Link from "next/link";

export default function Home() {
  return (
    <main className="flex min-h-screen items-center justify-center p-6">
      <div className="dusk-panel flex w-full max-w-md flex-col gap-5 p-8 text-center">
        <div
          className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl text-lg font-bold text-white"
          style={{ background: "linear-gradient(135deg, #6366f1, #818cf8)" }}
        >
          W
        </div>
        <div className="flex flex-col gap-1">
          <h1 className="text-xl font-semibold text-dusk-primary">
            Weekly Report Generator
          </h1>
          <p className="text-sm text-dusk-secondary">
            Structured weekly reports, manager review, and team dashboards.
          </p>
        </div>
        <div className="flex flex-col gap-2 sm:flex-row sm:justify-center">
          <Link
            href="/login"
            className="rounded-lg bg-foreground px-4 py-2 text-sm font-medium text-background"
          >
            Sign in
          </Link>
          <Link
            href="/register"
            className="rounded-lg border border-white/20 px-4 py-2 text-sm font-medium text-dusk-primary transition-colors hover:border-dusk-accent"
          >
            Create an account
          </Link>
        </div>
      </div>
    </main>
  );
}
