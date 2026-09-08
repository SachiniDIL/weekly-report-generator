"use client";

import { useQuery } from "@tanstack/react-query";
import { listReports } from "@/lib/api/reports";
import { useReviewQueueQuery } from "@/lib/reports/use-review-queue-query";
import type { AppShellVariant } from "./nav-config";

export interface NavBadge {
  /** A plain indicator that something needs attention. */
  dot?: boolean;
  /** A number to show (e.g. how many reports are waiting). */
  count?: number;
}

/**
 * Sidebar attention markers, keyed by nav href:
 * - member: a dot on "Reports" when any of their reports needs corrections
 * - manager: a count on "Review Queue" of reports awaiting review
 * Only the query relevant to the current variant runs.
 */
export function useNavBadges(
  variant: AppShellVariant,
): Record<string, NavBadge> {
  const myCorrections = useQuery({
    queryKey: ["reports", "my-corrections-count"],
    queryFn: () => listReports({ status: "NEEDS_CORRECTION", size: 1 }),
    enabled: variant === "member",
  });

  const reviewQueue = useReviewQueueQuery({ enabled: variant === "manager" });

  if (variant === "member" && (myCorrections.data?.totalElements ?? 0) > 0) {
    return { "/reports": { dot: true } };
  }
  if (variant === "manager") {
    const count = reviewQueue.data?.totalElements ?? 0;
    if (count > 0) {
      return { "/review": { count } };
    }
  }
  return {};
}
