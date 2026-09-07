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
import {
  CHART_ACCENT,
  CHART_AXIS_STROKE,
  CHART_GRID_STROKE,
  CHART_TICK,
  CHART_TOOLTIP_ITEM_STYLE,
  CHART_TOOLTIP_LABEL_STYLE,
  CHART_TOOLTIP_STYLE,
} from "./chart-palette";

export function WorkloadByProjectChart() {
  const query = useWorkloadByProjectQuery();

  return (
    <ChartCard title="Workload by project" query={query}>
      {(rows) => (
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={rows} layout="vertical">
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_GRID_STROKE} />
            <XAxis
              type="number"
              allowDecimals={false}
              tick={CHART_TICK}
              stroke={CHART_AXIS_STROKE}
            />
            <YAxis
              type="category"
              dataKey="projectName"
              width={110}
              tick={CHART_TICK}
              stroke={CHART_AXIS_STROKE}
            />
            <Tooltip
              contentStyle={CHART_TOOLTIP_STYLE}
              labelStyle={CHART_TOOLTIP_LABEL_STYLE}
              itemStyle={CHART_TOOLTIP_ITEM_STYLE}
              cursor={{ fill: "rgba(99, 102, 241, 0.08)" }}
            />
            <Bar
              dataKey="taskCount"
              fill={CHART_ACCENT}
              radius={[0, 4, 4, 0]}
            />
          </BarChart>
        </ResponsiveContainer>
      )}
    </ChartCard>
  );
}
