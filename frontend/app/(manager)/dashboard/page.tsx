"use client";

import { useState } from "react";
import { DashboardOverview } from "./dashboard-overview";
import { SectionComparison } from "./section-comparison";

const TABS = [
  { value: "overview", label: "Overview" },
  { value: "sections", label: "Section comparison" },
] as const;

type DashboardTab = (typeof TABS)[number]["value"];

export default function ManagerDashboardPage() {
  const [tab, setTab] = useState<DashboardTab>("overview");

  return (
    <main className="mx-auto flex max-w-5xl flex-col gap-6 p-6">
      <h1 className="text-xl font-semibold text-dusk-primary">
        Team dashboard
      </h1>

      <nav
        className="flex gap-1 border-b border-white/10"
        aria-label="Dashboard views"
      >
        {TABS.map((option) => {
          const selected = option.value === tab;
          return (
            <button
              key={option.value}
              type="button"
              role="tab"
              aria-selected={selected}
              onClick={() => setTab(option.value)}
              className={`-mb-px border-b-2 px-3 py-2 text-sm transition-colors ${
                selected
                  ? "border-dusk-accent font-medium text-dusk-accent-light"
                  : "border-transparent text-dusk-secondary hover:text-dusk-primary"
              }`}
            >
              {option.label}
            </button>
          );
        })}
      </nav>

      {tab === "overview" ? <DashboardOverview /> : <SectionComparison />}
    </main>
  );
}
