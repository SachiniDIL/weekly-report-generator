"use client";

import { Avatar } from "@/lib/avatar";
import { useSubmissionStatusQuery } from "@/lib/dashboard/dashboard-queries";
import { MemberWeekStatusChip } from "@/lib/dashboard/member-week-status-chip";
import { ChartCard } from "./chart-card";

export function SubmissionStatusList() {
  const query = useSubmissionStatusQuery();

  return (
    <ChartCard title="Submission status this week" query={query}>
      {(rows) => (
        <ul className="flex flex-col divide-y divide-black/5 text-sm">
          {rows.map((row) => (
            <li
              key={row.memberName}
              className="dusk-row flex items-center justify-between gap-2 px-2 py-2"
            >
              <span className="flex items-center gap-2 text-dusk-primary">
                <Avatar name={row.memberName} />
                {row.memberName}
              </span>
              <MemberWeekStatusChip status={row.status} />
            </li>
          ))}
        </ul>
      )}
    </ChartCard>
  );
}
