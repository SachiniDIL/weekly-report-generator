"use client";

import type { UseQueryResult } from "@tanstack/react-query";
import type { ReactNode } from "react";
import { describeError } from "@/lib/api-client";

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
    <section className="rounded border border-black/10 p-4 dark:border-white/15">
      <h2 className="mb-3 text-sm font-semibold">{title}</h2>
      {query.isPending ? (
        <p className="text-sm text-gray-500">Loading…</p>
      ) : query.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
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
