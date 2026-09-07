"use client";

import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { generateSummary } from "@/lib/api/ai";

export function TeamSummaryPanel() {
  const [dismissed, setDismissed] = useState(false);

  const mutation = useMutation({
    mutationFn: () => generateSummary(),
    onSuccess: () => setDismissed(false),
  });

  const summary = mutation.data?.message;

  return (
    <section className="flex flex-col gap-3 rounded border border-black/10 p-4 dark:border-white/15">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-sm font-semibold">AI team summary</h2>
        <button
          type="button"
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending}
          className="rounded bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-60"
        >
          {mutation.isPending ? "Generating…" : "Generate AI summary"}
        </button>
      </div>

      {mutation.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(mutation.error)}
        </p>
      ) : null}

      {summary && !dismissed ? (
        <div className="flex items-start justify-between gap-3 rounded bg-black/[.03] p-3 text-sm dark:bg-white/[.06]">
          <p className="whitespace-pre-wrap">{summary}</p>
          <button
            type="button"
            onClick={() => setDismissed(true)}
            aria-label="Dismiss summary"
            className="shrink-0 text-gray-500 hover:text-foreground"
          >
            ✕
          </button>
        </div>
      ) : null}
    </section>
  );
}
