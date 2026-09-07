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
    <nav
      className="flex items-center justify-between gap-4 text-sm"
      aria-label="Pagination"
    >
      <button
        type="button"
        onClick={onPrevious}
        disabled={pageNumber === 0}
        className="rounded-lg border border-white/20 px-3 py-1.5 text-dusk-primary transition-colors hover:border-dusk-accent disabled:opacity-40 hover:disabled:border-white/20"
      >
        Previous
      </button>
      <span className="text-dusk-secondary">
        Page {pageNumber + 1} of {totalPages}
      </span>
      <button
        type="button"
        onClick={onNext}
        disabled={pageNumber >= totalPages - 1}
        className="rounded-lg border border-white/20 px-3 py-1.5 text-dusk-primary transition-colors hover:border-dusk-accent disabled:opacity-40 hover:disabled:border-white/20"
      >
        Next
      </button>
    </nav>
  );
}
