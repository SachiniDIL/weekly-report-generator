"use client";

import { SubmissionStatusList } from "./submission-status-list";
import { SummaryCards } from "./summary-cards";
import { TasksCompletedChart } from "./tasks-completed-chart";
import { TimeByTaskTypeChart } from "./time-by-task-type-chart";
import { WorkloadByProjectChart } from "./workload-by-project-chart";

export function DashboardOverview() {
  return (
    <div className="flex flex-col gap-6">
      <SummaryCards />
      <div className="grid gap-6 lg:grid-cols-2">
        <TasksCompletedChart />
        <SubmissionStatusList />
        <WorkloadByProjectChart />
        <TimeByTaskTypeChart />
      </div>
    </div>
  );
}
