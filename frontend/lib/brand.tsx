/** The app wordmark — "Weekly" in primary text, "Report" in the indigo accent. */
export function BrandMark({ className = "" }: { className?: string }) {
  return (
    <span
      className={`font-semibold tracking-tight text-dusk-primary ${className}`}
    >
      Weekly<span className="text-dusk-accent-light">Report</span>
    </span>
  );
}

/** A 1px indigo gradient rule — used under the wordmark on the auth cards. */
export function BrandRule({ className = "" }: { className?: string }) {
  return (
    <span
      aria-hidden
      className={`block h-px w-full ${className}`}
      style={{
        background:
          "linear-gradient(90deg, transparent, #6366f1 40%, #a5b4fc 60%, transparent)",
      }}
    />
  );
}
