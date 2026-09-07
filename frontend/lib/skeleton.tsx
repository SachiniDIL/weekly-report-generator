/** Shimmer placeholders for loading states. `aria-hidden` — the surrounding
 * region carries the accessible "loading" text. */
export function Skeleton({
  className = "",
  style,
}: {
  className?: string;
  style?: React.CSSProperties;
}) {
  return (
    <div aria-hidden className={`dusk-skeleton ${className}`} style={style} />
  );
}

/** A few staggered lines of shimmer, for a text block that's loading. */
export function SkeletonText({
  lines = 3,
  className = "",
}: {
  lines?: number;
  className?: string;
}) {
  return (
    <div
      className={`flex flex-col gap-2 ${className}`}
      role="status"
      aria-label="Loading"
    >
      {Array.from({ length: lines }).map((_, index) => (
        <Skeleton
          key={index}
          className="h-3.5"
          style={{ width: `${Math.max(40, 92 - index * 14)}%` }}
        />
      ))}
    </div>
  );
}

/** A shimmer card matching the panel footprint, for a loading list/chart area. */
export function SkeletonCard({
  className = "",
  height = 96,
}: {
  className?: string;
  height?: number;
}) {
  return (
    <div role="status" aria-label="Loading">
      <Skeleton className={className} style={{ height }} />
    </div>
  );
}
