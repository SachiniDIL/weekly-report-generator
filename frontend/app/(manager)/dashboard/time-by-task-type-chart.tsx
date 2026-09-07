"use client";

import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from "recharts";
import { useTimeByTaskTypeQuery } from "@/lib/dashboard/dashboard-queries";
import { ChartCard } from "./chart-card";
import {
  CHART_PALETTE,
  CHART_TOOLTIP_ITEM_STYLE,
  CHART_TOOLTIP_LABEL_STYLE,
  CHART_TOOLTIP_STYLE,
} from "./chart-palette";

export function TimeByTaskTypeChart() {
  const query = useTimeByTaskTypeQuery();

  return (
    <ChartCard title="Time by task type" query={query}>
      {(rows) => (
        <ResponsiveContainer width="100%" height={260}>
          <PieChart>
            <Pie
              data={rows}
              dataKey="totalHours"
              nameKey="taskType"
              outerRadius={90}
              label
              stroke="#111827"
            >
              {rows.map((row, index) => (
                <Cell
                  key={row.taskType}
                  fill={CHART_PALETTE[index % CHART_PALETTE.length]}
                />
              ))}
            </Pie>
            <Tooltip
              contentStyle={CHART_TOOLTIP_STYLE}
              labelStyle={CHART_TOOLTIP_LABEL_STYLE}
              itemStyle={CHART_TOOLTIP_ITEM_STYLE}
            />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      )}
    </ChartCard>
  );
}
