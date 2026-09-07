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
import type { WeeklyTaskCompletionPoint } from "@/lib/api/dashboard";
import { useTasksCompletedTrendQuery } from "@/lib/dashboard/dashboard-queries";
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

export function TasksCompletedChart() {
  const query = useTasksCompletedTrendQuery();

  return (
    <ChartCard title="Tasks completed by week" query={query}>
      {(rows) => (
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={rows.map(toBar)}>
            <CartesianGrid strokeDasharray="3 3" stroke={CHART_GRID_STROKE} />
            <XAxis
              dataKey="week"
              tick={CHART_TICK}
              stroke={CHART_AXIS_STROKE}
            />
            <YAxis
              allowDecimals={false}
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
              dataKey="completedTasks"
              fill={CHART_ACCENT}
              radius={[4, 4, 0, 0]}
            />
          </BarChart>
        </ResponsiveContainer>
      )}
    </ChartCard>
  );
}

function toBar(row: WeeklyTaskCompletionPoint) {
  return { week: row.weekStart, completedTasks: row.completedTasks };
}
