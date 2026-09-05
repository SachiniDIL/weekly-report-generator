import type { ReviewCommentView } from "@/lib/api/reports";

/** The manager's "please fix this" comment, shown prominently while a report is being corrected. */
export function CorrectionNotice({ comment }: { comment: ReviewCommentView }) {
  return (
    <aside
      role="alert"
      className="rounded border border-amber-300 bg-amber-50 p-4 text-sm dark:border-amber-800 dark:bg-amber-950"
    >
      <p className="font-semibold text-amber-800 dark:text-amber-200">Changes requested</p>
      <p className="mt-1 whitespace-pre-wrap text-amber-900 dark:text-amber-100">{comment.comment}</p>
      <p className="mt-2 text-xs text-amber-700 dark:text-amber-300">— {comment.managerName}</p>
    </aside>
  );
}
