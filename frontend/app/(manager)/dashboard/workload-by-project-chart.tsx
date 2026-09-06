"use client";

import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { useWorkloadByProjectQuery } from "@/lib/dashboard/dashboard-queries";
import { ChartCard } from "./chart-card";
import { CHART_ACCENT } from "./chart-palette";

export function WorkloadByProjectChart() {
  const query = useWorkloadByProjectQuery();

  return (
    <ChartCard title="Workload by project" query={query}>
      {(rows) => (
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={rows} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis type="number" allowDecimals={false} fontSize={12} />
            <YAxis type="category" dataKey="projectName" width={110} fontSize={12} />
            <Tooltip />
            <Bar dataKey="taskCount" fill={CHART_ACCENT} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </ChartCard>
  );
}
