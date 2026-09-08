"use client";

import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { generateSummary } from "@/lib/api/ai";
import { AiMarkdown } from "@/lib/ai/ai-markdown";

export function TeamSummaryPanel() {
  const [dismissed, setDismissed] = useState(false);

  const mutation = useMutation({
    mutationFn: () => generateSummary(),
    onSuccess: () => setDismissed(false),
  });

  const summary = mutation.data?.message;

  return (
    <section className="dusk-panel flex flex-col gap-3 p-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-sm font-semibold text-dusk-primary">
          AI team summary
        </h2>
        <button
          type="button"
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending}
          className="rounded-lg bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-60"
        >
          {mutation.isPending ? "Generating…" : "Generate AI summary"}
        </button>
      </div>

      {mutation.isError ? (
        <p role="alert" className="dusk-banner-error px-3 py-2 text-sm">
          {describeError(mutation.error)}
        </p>
      ) : null}

      {summary && !dismissed ? (
        <div className="dusk-banner flex items-start justify-between gap-3 rounded-lg p-4 text-sm">
          <div className="min-w-0 flex-1">
            <AiMarkdown text={summary} />
          </div>
          <button
            type="button"
            onClick={() => setDismissed(true)}
            aria-label="Dismiss summary"
            className="shrink-0 text-dusk-secondary hover:text-dusk-accent-light"
          >
            ✕
          </button>
        </div>
      ) : null}
    </section>
  );
}
