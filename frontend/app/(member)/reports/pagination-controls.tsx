export interface PaginationControlsProps {
  /** Zero-based, matching Spring's Page.number. */
  pageNumber: number;
  totalPages: number;
  onPrevious: () => void;
  onNext: () => void;
}

export function PaginationControls({
  pageNumber,
  totalPages,
  onPrevious,
  onNext,
}: PaginationControlsProps) {
  if (totalPages <= 1) {
    return null;
  }

  return (
    <nav className="flex items-center justify-between gap-4 text-sm" aria-label="Pagination">
      <button
        type="button"
        onClick={onPrevious}
        disabled={pageNumber === 0}
        className="rounded border border-black/20 px-3 py-1.5 disabled:opacity-40 dark:border-white/25"
      >
        Previous
      </button>
      <span className="text-gray-500">
        Page {pageNumber + 1} of {totalPages}
      </span>
      <button
        type="button"
        onClick={onNext}
        disabled={pageNumber >= totalPages - 1}
        className="rounded border border-black/20 px-3 py-1.5 disabled:opacity-40 dark:border-white/25"
      >
        Next
      </button>
    </nav>
  );
}
