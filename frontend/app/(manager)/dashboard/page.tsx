"use client";

import { SubmissionStatusList } from "./submission-status-list";
import { SummaryCards } from "./summary-cards";
import { TasksCompletedChart } from "./tasks-completed-chart";
import { TimeByTaskTypeChart } from "./time-by-task-type-chart";
import { WorkloadByProjectChart } from "./workload-by-project-chart";

export default function ManagerDashboardPage() {
  return (
    <main className="mx-auto flex max-w-5xl flex-col gap-6 p-6">
      <h1 className="text-xl font-semibold">Team dashboard</h1>
      <SummaryCards />
      <div className="grid gap-6 lg:grid-cols-2">
        <TasksCompletedChart />
        <SubmissionStatusList />
        <WorkloadByProjectChart />
        <TimeByTaskTypeChart />
      </div>
    </main>
  );
}
