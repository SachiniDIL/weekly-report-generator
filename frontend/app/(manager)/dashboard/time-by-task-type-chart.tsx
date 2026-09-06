"use client";

import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { useTimeByTaskTypeQuery } from "@/lib/dashboard/dashboard-queries";
import { ChartCard } from "./chart-card";
import { CHART_PALETTE } from "./chart-palette";

export function TimeByTaskTypeChart() {
  const query = useTimeByTaskTypeQuery();

  return (
    <ChartCard title="Time by task type" query={query}>
      {(rows) => (
        <ResponsiveContainer width="100%" height={260}>
          <PieChart>
            <Pie data={rows} dataKey="totalHours" nameKey="taskType" outerRadius={90} label>
              {rows.map((row, index) => (
                <Cell key={row.taskType} fill={CHART_PALETTE[index % CHART_PALETTE.length]} />
              ))}
            </Pie>
            <Tooltip />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      )}
    </ChartCard>
  );
}
