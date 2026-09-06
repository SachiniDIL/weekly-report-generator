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
import { CHART_ACCENT } from "./chart-palette";

export function TasksCompletedChart() {
  const query = useTasksCompletedTrendQuery();

  return (
    <ChartCard title="Tasks completed by week" query={query}>
      {(rows) => (
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={rows.map(toBar)}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="week" fontSize={12} />
            <YAxis allowDecimals={false} fontSize={12} />
            <Tooltip />
            <Bar dataKey="completedTasks" fill={CHART_ACCENT} />
          </BarChart>
        </ResponsiveContainer>
      )}
    </ChartCard>
  );
}

function toBar(row: WeeklyTaskCompletionPoint) {
  return { week: row.weekStart, completedTasks: row.completedTasks };
}
