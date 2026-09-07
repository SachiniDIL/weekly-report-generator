import type { ReviewCommentView } from "@/lib/api/reports";

/** The manager's "please fix this" comment, shown prominently while a report is being corrected. */
export function CorrectionNotice({ comment }: { comment: ReviewCommentView }) {
  return (
    <aside
      role="alert"
      className="rounded-xl border border-amber-400/40 bg-amber-950/40 p-4 text-sm"
    >
      <p className="font-semibold text-amber-200">Changes requested</p>
      <p className="mt-1 whitespace-pre-wrap text-amber-100">
        {comment.comment}
      </p>
      <p className="mt-2 text-xs text-amber-300">— {comment.managerName}</p>
    </aside>
  );
}
