import {
  AlertTriangle,
  ArrowRight,
  CheckSquare,
  Clock,
  FileText,
  Link2,
  Star,
  type LucideIcon,
} from "lucide-react";
import type { ReportContentResponse, ReportResponse } from "@/lib/api/reports";
import { Avatar } from "@/lib/avatar";
import { ReportStatusBadge } from "@/lib/reports/report-status-badge";
import {
  priorityLabel,
  priorityTone,
  statusLabel,
  statusTone,
  type ChipTone,
} from "@/lib/reports/task-fields";

/** Read-only presentation of a report — the review page, the shared view route, and a report a member can no longer edit. */
export function ReportContentView({ report }: { report: ReportResponse }) {
  return (
    <div className="dusk-panel flex flex-col gap-6 p-4 sm:p-6">
      <header className="flex flex-col gap-2">
        <h1 className="text-xl font-semibold text-dusk-primary">
          {report.projectName}
        </h1>
        <div className="flex flex-wrap items-center gap-x-3 gap-y-2 text-sm text-dusk-secondary">
          <span>
            Week of {report.weekStart} &ndash; {report.weekEnd}
          </span>
          <span className="text-dusk-muted">·</span>
          <span className="flex items-center gap-1.5">
            <Avatar name={report.ownerName} size={18} />
            {report.ownerName}
          </span>
          <ReportStatusBadge status={report.status} />
          <span className="text-dusk-muted">
            version {report.currentVersionNo}
          </span>
        </div>
      </header>

      <ReportContentBody content={report.content} />
    </div>
  );
}

/** Just the content sections of one version — shared by the current-report view and each version in the history. */
export function ReportContentBody({
  content,
}: {
  content: ReportContentResponse;
}) {
  return (
    <div className="flex flex-col gap-7">
      <Section title="Tasks" icon={CheckSquare}>
        {content.taskEntries.length === 0 ? (
          <Empty>No tasks recorded.</Empty>
        ) : (
          <ul className="flex flex-col gap-2.5">
            {content.taskEntries.map((entry) => (
              <TaskCard key={entry.id} entry={entry} />
            ))}
          </ul>
        )}
      </Section>

      <Section title="Blockers" icon={AlertTriangle}>
        <FlaggedList
          items={content.blockers}
          flag="isKeyIssue"
          flagLabel="Key issue"
          emptyLabel="No blockers."
        />
      </Section>

      <Section title="Achievements" icon={Star}>
        <FlaggedList
          items={content.achievements}
          flag="isKeyHighlight"
          flagLabel="Key highlight"
          emptyLabel="No achievements recorded."
        />
      </Section>

      <Section title="Hours breakdown" icon={Clock}>
        <Hours rows={content.hoursBreakdown} />
      </Section>

      <Section title="Notes" icon={FileText}>
        <Prose value={content.notes} />
      </Section>

      <Section title="Links" icon={Link2}>
        <Links value={content.links} />
      </Section>

      <Section title="Planned for next week" icon={ArrowRight}>
        <Prose value={content.tasksPlannedNext} />
      </Section>
    </div>
  );
}

type TaskEntry = ReportContentResponse["taskEntries"][number];

function TaskCard({ entry }: { entry: TaskEntry }) {
  const actual = clampPct(entry.actualPct);
  return (
    <li className="flex flex-col gap-2 rounded-xl bg-black/[0.03] p-3">
      <div className="flex flex-wrap items-start justify-between gap-x-3 gap-y-1.5">
        <p className="font-medium text-dusk-primary">{entry.taskName}</p>
        <div className="flex shrink-0 flex-wrap gap-1.5">
          <Chip tone={priorityTone(entry.priority)}>
            {priorityLabel(entry.priority)}
          </Chip>
          <Chip tone={statusTone(entry.status)}>
            {statusLabel(entry.status)}
          </Chip>
        </div>
      </div>

      <div className="flex flex-col gap-1">
        <div className="flex flex-wrap gap-x-4 gap-y-0.5 text-xs text-dusk-secondary">
          <span>
            Planned{" "}
            <b className="font-medium text-dusk-primary">{entry.plannedPct}%</b>
          </span>
          <span>
            Actual{" "}
            <b className="font-medium text-dusk-primary">{entry.actualPct}%</b>
          </span>
          {entry.timeSpent != null ? (
            <span>
              Time spent{" "}
              <b className="font-medium text-dusk-primary">
                {entry.timeSpent}h
              </b>
            </span>
          ) : null}
        </div>
        <div className="h-1.5 overflow-hidden rounded-full bg-black/[0.06]">
          <div
            className="h-full rounded-full"
            style={{ width: `${actual}%`, background: "var(--accent)" }}
          />
        </div>
      </div>

      {entry.deliverable ? (
        <p className="text-sm text-dusk-secondary">
          <span className="text-dusk-muted">Deliverable — </span>
          {entry.deliverable}
        </p>
      ) : null}
    </li>
  );
}

function Hours({ rows }: { rows: ReportContentResponse["hoursBreakdown"] }) {
  if (rows.length === 0) {
    return <Empty>No hours logged.</Empty>;
  }
  const total = rows.reduce((sum, row) => sum + Number(row.hours), 0);
  return (
    <dl className="flex flex-col text-sm">
      {rows.map((row) => (
        <div
          key={row.id}
          className="flex justify-between border-b border-dusk-border/60 py-1.5"
        >
          <dt className="text-dusk-secondary">{row.taskType}</dt>
          <dd className="tabular-nums text-dusk-primary">{row.hours}h</dd>
        </div>
      ))}
      <div className="flex justify-between py-1.5 font-medium text-dusk-primary">
        <dt>Total</dt>
        <dd className="tabular-nums">{total}h</dd>
      </div>
    </dl>
  );
}

function FlaggedList<Item extends { id: number; description: string }>({
  items,
  flag,
  flagLabel,
  emptyLabel,
}: {
  items: Item[];
  flag: keyof Item;
  flagLabel: string;
  emptyLabel: string;
}) {
  if (items.length === 0) {
    return <Empty>{emptyLabel}</Empty>;
  }
  return (
    <ul className="flex flex-col gap-1.5 text-sm">
      {items.map((item) => {
        const isKey = Boolean(item[flag]);
        return (
          <li key={item.id} className="flex items-start gap-2">
            {isKey ? <Chip tone="warning">{flagLabel}</Chip> : null}
            <span
              className={
                isKey ? "font-medium text-dusk-primary" : "text-dusk-secondary"
              }
            >
              {item.description}
            </span>
          </li>
        );
      })}
    </ul>
  );
}

const TONE_STYLE: Record<ChipTone, React.CSSProperties> = {
  neutral: { color: "#5b6478", background: "rgba(57, 65, 90, 0.09)" },
  info: { color: "#4338ca", background: "rgba(91, 99, 230, 0.14)" },
  warning: { color: "#b45309", background: "rgba(217, 123, 31, 0.16)" },
  danger: { color: "#b91c1c", background: "rgba(220, 76, 76, 0.13)" },
  success: { color: "#1d7a46", background: "rgba(47, 158, 95, 0.16)" },
};

function Chip({
  tone,
  children,
}: {
  tone: ChipTone;
  children: React.ReactNode;
}) {
  return (
    <span
      className="inline-flex shrink-0 items-center rounded-full px-2 py-0.5 text-xs font-medium"
      style={TONE_STYLE[tone]}
    >
      {children}
    </span>
  );
}

function Section({
  title,
  icon: Icon,
  children,
}: {
  title: string;
  icon: LucideIcon;
  children: React.ReactNode;
}) {
  return (
    <section className="flex flex-col gap-2.5" aria-label={title}>
      <h2 className="dusk-section-heading">
        <Icon size={14} aria-hidden />
        {title}
      </h2>
      {children}
    </section>
  );
}

function Prose({ value }: { value: string | null }) {
  if (!value?.trim()) {
    return <Empty>Nothing noted.</Empty>;
  }
  return (
    <p className="whitespace-pre-wrap break-words text-sm text-dusk-primary">
      {value}
    </p>
  );
}

function Links({ value }: { value: string | null }) {
  if (!value?.trim()) {
    return <Empty>No links.</Empty>;
  }
  return (
    <p className="break-words text-sm text-dusk-primary">
      {value.split(/(\s+)/).map((token, index) =>
        /^https?:\/\/\S+$/i.test(token) ? (
          <a
            key={index}
            href={token}
            target="_blank"
            rel="noopener noreferrer"
            className="break-all text-dusk-accent-light underline"
          >
            {token}
          </a>
        ) : (
          <span key={index}>{token}</span>
        ),
      )}
    </p>
  );
}

function Empty({ children }: { children?: React.ReactNode }) {
  return <p className="text-sm text-dusk-muted">{children ?? "None."}</p>;
}

function clampPct(value: number): number {
  return Math.max(0, Math.min(100, value));
}
