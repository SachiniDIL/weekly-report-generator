import type { ReportVersionHistoryItem, ReviewCommentView } from "@/lib/api/reports";
import { ReportContentBody } from "./report-content-view";

/** Prior versions of a report, newest-first, each with the manager comment made against it. */
export function VersionHistoryList({ versions }: { versions: ReportVersionHistoryItem[] }) {
  return (
    <ol className="flex flex-col gap-8">
      {versions.map((version) => (
        <li
          key={version.content.reportVersionId}
          className="flex flex-col gap-4 rounded border border-black/10 p-4 dark:border-white/15"
        >
          <h3 className="text-sm font-semibold uppercase tracking-wide text-gray-500">
            Version {version.content.versionNo}
          </h3>
          {version.reviewComment ? <VersionReviewComment comment={version.reviewComment} /> : null}
          <ReportContentBody content={version.content} />
        </li>
      ))}
    </ol>
  );
}

function VersionReviewComment({ comment }: { comment: ReviewCommentView }) {
  const label = comment.action === "APPROVED" ? "Approved" : "Changes requested";
  return (
    <div className="rounded bg-black/5 p-3 text-sm dark:bg-white/10">
      <p className="font-medium">
        {label} by {comment.managerName}
      </p>
      {comment.comment ? <p className="mt-1 whitespace-pre-wrap">{comment.comment}</p> : null}
    </div>
  );
}
