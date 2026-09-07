import { SkeletonText } from "@/lib/skeleton";

/** A status/error line for the report pages' loading and access states.
 * `tone="loading"` shows a shimmer block instead of plain text. */
export function ReportMessage({
  tone = "muted",
  children,
}: {
  tone?: "muted" | "error" | "loading";
  children?: React.ReactNode;
}) {
  if (tone === "loading") {
    return (
      <div className="p-6">
        <SkeletonText lines={4} />
      </div>
    );
  }

  return (
    <p
      className={`p-6 text-sm ${tone === "error" ? "text-red-700" : "text-dusk-secondary"}`}
      role={tone === "error" ? "alert" : undefined}
    >
      {children}
    </p>
  );
}
