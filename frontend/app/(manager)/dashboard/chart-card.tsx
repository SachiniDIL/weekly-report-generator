"use client";

import type { UseQueryResult } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { describeError } from "@/lib/api-client";
import { Skeleton } from "@/lib/skeleton";

/** Titled panel that renders one loading/error/empty state before handing rows to the chart. */
export function ChartCard<Row>({
  title,
  query,
  children,
}: {
  title: string;
  query: UseQueryResult<Row[]>;
  children: (rows: Row[]) => ReactNode;
}) {
  return (
    <section className="dusk-panel p-4">
      <h2
        className="mb-3 text-sm font-semibold text-dusk-primary"
        style={{
          paddingBottom: "0.5rem",
          borderBottom: "1px solid transparent",
          borderImage: "linear-gradient(90deg, #6366f1, transparent) 1",
        }}
      >
        {title}
      </h2>
      {query.isPending ? (
        <div role="status" aria-label="Loading" className="flex flex-col gap-2">
          <Skeleton className="h-40 w-full" />
          <Skeleton className="h-3 w-1/2" />
        </div>
      ) : query.isError ? (
        <p role="alert" className="text-sm text-red-700">
          {describeError(query.error)}
        </p>
      ) : query.data.length === 0 ? (
        <p className="text-sm text-gray-500">No data for this week.</p>
      ) : (
        children(query.data)
      )}
    </section>
  );
}
