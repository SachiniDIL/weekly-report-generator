import type { ReportVersionHistoryItem, ReviewCommentView } from "@/lib/api/reports";

/**
 * The comment a member needs to see while fixing a NEEDS_CORRECTION report lives on the
 * second-to-latest version — the one that existed when the manager asked for changes, before
 * the correction cycle opened the new editable version. History comes back newest-first.
 */
export function findCorrectionComment(
  history: ReportVersionHistoryItem[],
): ReviewCommentView | null {
  const commentedVersion = history[1];
  if (commentedVersion?.reviewComment?.action === "CHANGES_REQUESTED") {
    return commentedVersion.reviewComment;
  }
  return null;
}
