"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import type { ReviewAction } from "@/lib/api/reports";
import { useReviewReportMutation } from "@/lib/reports/use-review-report-mutation";
import { validateReviewInput } from "./review-form";

export function ReviewActionsForm({ reportId }: { reportId: number }) {
  const [action, setAction] = useState<ReviewAction>("APPROVED");
  const [comment, setComment] = useState("");
  const [clientError, setClientError] = useState<string | null>(null);
  const mutation = useReviewReportMutation(reportId);

  const commentRequired = action === "CHANGES_REQUESTED";

  function selectAction(next: ReviewAction) {
    setAction(next);
    setClientError(null);
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const problem = validateReviewInput({ action, comment });
    setClientError(problem);
    if (!problem) {
      mutation.mutate({ action, comment: comment.trim() === "" ? null : comment.trim() });
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-4 rounded border border-black/15 p-4 dark:border-white/20"
    >
      <h2 className="text-lg font-semibold">Review</h2>

      <fieldset className="flex flex-col gap-2">
        <legend className="text-sm font-medium">Decision</legend>
        <label className="flex items-center gap-2 text-sm">
          <input
            type="radio"
            name="review-action"
            checked={action === "APPROVED"}
            onChange={() => selectAction("APPROVED")}
          />
          Approve
        </label>
        <label className="flex items-center gap-2 text-sm">
          <input
            type="radio"
            name="review-action"
            checked={action === "CHANGES_REQUESTED"}
            onChange={() => selectAction("CHANGES_REQUESTED")}
          />
          Request changes
        </label>
      </fieldset>

      <div className="flex flex-col gap-1">
        <label htmlFor="review-comment" className="text-sm font-medium">
          {commentRequired ? "What needs to change" : "Comment (optional)"}
        </label>
        <textarea
          id="review-comment"
          rows={3}
          value={comment}
          onChange={(event) => {
            setComment(event.target.value);
            setClientError(null);
          }}
          className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm outline-none focus:border-black/40 dark:border-white/20"
        />
      </div>

      {clientError ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {clientError}
        </p>
      ) : null}
      {mutation.isError ? (
        <p
          role="alert"
          className="rounded bg-red-50 p-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300"
        >
          {describeError(mutation.error)}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={mutation.isPending}
        className="self-start rounded bg-foreground px-4 py-2 text-sm font-medium text-background disabled:opacity-60"
      >
        {mutation.isPending ? "Submitting…" : commentRequired ? "Request changes" : "Approve"}
      </button>
    </form>
  );
}
