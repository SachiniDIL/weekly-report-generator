"use client";

import { useState } from "react";
import type {
  DashboardSectionName,
  MemberSectionView,
  SectionItem,
} from "@/lib/api/dashboard";
import { Avatar } from "@/lib/avatar";
import { currentIsoWeek, type IsoWeek } from "@/lib/dashboard/current-iso-week";
import { useSectionComparisonQuery } from "@/lib/dashboard/dashboard-queries";
import { MemberWeekStatusChip } from "@/lib/dashboard/member-week-status-chip";
import { ChartCard } from "./chart-card";

const SECTION_OPTIONS: { value: DashboardSectionName; label: string }[] = [
  { value: "blockers", label: "Blockers" },
  { value: "achievements", label: "Achievements" },
];

const KEY_ITEM_LABEL: Record<DashboardSectionName, string> = {
  blockers: "Key issue",
  achievements: "Key highlight",
};

export function SectionComparison() {
  const [section, setSection] = useState<DashboardSectionName>("blockers");
  const [week, setWeek] = useState<IsoWeek>(() => currentIsoWeek());
  const query = useSectionComparisonQuery({ section, ...week });

  return (
    <section className="flex flex-col gap-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <SectionToggle value={section} onChange={setSection} />
        <WeekSelector value={week} onChange={setWeek} />
      </div>

      <ChartCard
        title={`${sectionTitle(section)} across the team`}
        query={query}
      >
        {(members) => (
          <ul className="flex flex-col divide-y divide-black/10">
            {members.map((member) => (
              <MemberRow
                key={member.memberName}
                member={member}
                section={section}
              />
            ))}
          </ul>
        )}
      </ChartCard>
    </section>
  );
}

function sectionTitle(section: DashboardSectionName): string {
  return section === "blockers" ? "Blockers" : "Achievements";
}

function SectionToggle({
  value,
  onChange,
}: {
  value: DashboardSectionName;
  onChange: (next: DashboardSectionName) => void;
}) {
  return (
    <div className="flex gap-2" role="group" aria-label="Section">
      {SECTION_OPTIONS.map((option) => {
        const selected = option.value === value;
        return (
          <button
            key={option.value}
            type="button"
            aria-pressed={selected}
            onClick={() => onChange(option.value)}
            className={`rounded-lg px-3 py-1.5 text-sm transition-colors ${
              selected
                ? "bg-foreground text-background"
                : "border border-white/20 text-dusk-secondary hover:text-dusk-accent-light"
            }`}
          >
            {option.label}
          </button>
        );
      })}
    </div>
  );
}

function WeekSelector({
  value,
  onChange,
}: {
  value: IsoWeek;
  onChange: (next: IsoWeek) => void;
}) {
  return (
    <div className="flex items-center gap-2 text-sm">
      <label className="flex items-center gap-1">
        <span className="text-gray-500">Week of</span>
        <input
          type="date"
          aria-label="Week start"
          value={value.weekStart}
          onChange={(event) =>
            onChange({ ...value, weekStart: event.target.value })
          }
          className="rounded border border-black/15 bg-transparent px-2 py-1 dark:border-white/20"
        />
      </label>
      <label className="flex items-center gap-1">
        <span className="text-gray-500">to</span>
        <input
          type="date"
          aria-label="Week end"
          value={value.weekEnd}
          onChange={(event) =>
            onChange({ ...value, weekEnd: event.target.value })
          }
          className="rounded border border-black/15 bg-transparent px-2 py-1 dark:border-white/20"
        />
      </label>
    </div>
  );
}

function MemberRow({
  member,
  section,
}: {
  member: MemberSectionView;
  section: DashboardSectionName;
}) {
  return (
    <li className="dusk-row flex flex-col gap-2 px-2 py-3">
      <div className="flex items-center justify-between gap-2">
        <span className="flex items-center gap-2 text-sm font-medium text-dusk-primary">
          <Avatar name={member.memberName} />
          {member.memberName}
        </span>
        <MemberWeekStatusChip status={member.status} />
      </div>
      {member.items.length === 0 ? (
        <p className="text-sm text-gray-500">
          No {section} recorded for this week.
        </p>
      ) : (
        <ul className="flex flex-col gap-1.5">
          {member.items.map((item, index) => (
            <SectionItemRow key={index} item={item} section={section} />
          ))}
        </ul>
      )}
    </li>
  );
}

function SectionItemRow({
  item,
  section,
}: {
  item: SectionItem;
  section: DashboardSectionName;
}) {
  if (!item.key) {
    return <li className="text-sm text-dusk-secondary">{item.description}</li>;
  }
  return (
    <li className="flex items-start gap-2 rounded-lg border-l-4 border-amber-400/40 bg-amber-950/40 px-3 py-1.5 text-sm">
      <span className="shrink-0 rounded bg-amber-500 px-1.5 py-0.5 text-xs font-semibold text-[#1a1205]">
        {KEY_ITEM_LABEL[section]}
      </span>
      <span className="font-medium text-dusk-primary">{item.description}</span>
    </li>
  );
}
