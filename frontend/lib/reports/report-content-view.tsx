import type { ReportResponse } from "@/lib/api/reports";

/** Read-only presentation of a report — used for the shared view route and for a report the member can no longer edit. */
export function ReportContentView({ report }: { report: ReportResponse }) {
  const { content } = report;

  return (
    <div className="flex flex-col gap-6">
      <header className="flex flex-col gap-1">
        <h1 className="text-xl font-semibold">
          {report.projectName} — week of {report.weekStart} to {report.weekEnd}
        </h1>
        <p className="text-sm text-gray-500">
          {report.ownerName} · {report.status} · v{report.currentVersionNo}
        </p>
      </header>

      <TextBlock label="Planned for next week" value={content.tasksPlannedNext} />
      <TextBlock label="Notes" value={content.notes} />
      <TextBlock label="Links" value={content.links} />

      <Section title="Task entries">
        {content.taskEntries.length === 0 ? (
          <Empty />
        ) : (
          <ul className="flex flex-col gap-2">
            {content.taskEntries.map((entry) => (
              <li key={entry.id} className="rounded border border-black/10 p-3 text-sm dark:border-white/15">
                <p className="font-medium">{entry.taskName}</p>
                <p className="text-gray-500">
                  {entry.priority} · {entry.status} · planned {entry.plannedPct}% / actual{" "}
                  {entry.actualPct}%
                </p>
                {entry.deliverable ? <p className="mt-1">{entry.deliverable}</p> : null}
              </li>
            ))}
          </ul>
        )}
      </Section>

      <Section title="Blockers">
        <FlaggedList items={content.blockers} flag="isKeyIssue" flagLabel="key issue" />
      </Section>

      <Section title="Achievements">
        <FlaggedList items={content.achievements} flag="isKeyHighlight" flagLabel="key highlight" />
      </Section>

      <Section title="Hours breakdown">
        {content.hoursBreakdown.length === 0 ? (
          <Empty />
        ) : (
          <ul className="flex flex-col gap-1 text-sm">
            {content.hoursBreakdown.map((row) => (
              <li key={row.id}>
                {row.taskType}: {row.hours}h
              </li>
            ))}
          </ul>
        )}
      </Section>
    </div>
  );
}

function FlaggedList<Item extends { id: number; description: string }>({
  items,
  flag,
  flagLabel,
}: {
  items: Item[];
  flag: keyof Item;
  flagLabel: string;
}) {
  if (items.length === 0) {
    return <Empty />;
  }
  return (
    <ul className="flex flex-col gap-1 text-sm">
      {items.map((item) => (
        <li key={item.id}>
          {item.description}
          {item[flag] ? <span className="ml-2 text-amber-600">({flagLabel})</span> : null}
        </li>
      ))}
    </ul>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="flex flex-col gap-2">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">{title}</h2>
      {children}
    </section>
  );
}

function TextBlock({ label, value }: { label: string; value: string | null }) {
  return (
    <div className="flex flex-col gap-1">
      <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">{label}</h2>
      <p className="whitespace-pre-wrap text-sm">{value ?? "—"}</p>
    </div>
  );
}

function Empty() {
  return <p className="text-sm text-gray-500">None.</p>;
}
