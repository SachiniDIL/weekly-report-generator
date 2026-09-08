import type {
  ReportVersionHistoryItem,
  ReviewCommentView,
} from "@/lib/api/reports";
import { ReportContentBody } from "./report-content-view";

/** Prior versions of a report, newest-first, each with the manager comment made against it. */
export function VersionHistoryList({
  versions,
}: {
  versions: ReportVersionHistoryItem[];
}) {
  return (
    <ol className="flex flex-col gap-4">
      {versions.map((version) => (
        <li
          key={version.content.reportVersionId}
          className="dusk-panel flex flex-col gap-4 p-4"
        >
          <h3 className="text-xs font-semibold uppercase tracking-wide text-dusk-muted">
            Version {version.content.versionNo}
          </h3>
          {version.reviewComment ? (
            <VersionReviewComment comment={version.reviewComment} />
          ) : null}
          <ReportContentBody content={version.content} />
        </li>
      ))}
    </ol>
  );
}

function VersionReviewComment({ comment }: { comment: ReviewCommentView }) {
  const changesRequested = comment.action === "CHANGES_REQUESTED";
  return (
    <div
      className={`rounded-lg p-3 text-sm ${
        changesRequested ? "dusk-banner-error" : "dusk-banner-success"
      }`}
    >
      <p className="font-medium">
        {changesRequested ? "Changes requested" : "Approved"} by{" "}
        {comment.managerName}
      </p>
      {comment.comment ? (
        <p className="mt-1 whitespace-pre-wrap break-words">
          {comment.comment}
        </p>
      ) : null}
    </div>
  );
}
