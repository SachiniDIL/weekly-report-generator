/** A single-line status/error line for the report pages' loading and access states. */
export function ReportMessage({
  tone = "muted",
  children,
}: {
  tone?: "muted" | "error";
  children: React.ReactNode;
}) {
  return (
    <p
      className={`p-6 text-sm ${tone === "error" ? "text-red-700" : "text-gray-500"}`}
      role={tone === "error" ? "alert" : undefined}
    >
      {children}
    </p>
  );
}
