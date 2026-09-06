import Link from "next/link";
import type { ReportListItemView } from "@/lib/api/reports";
import { ReportStatusBadge } from "@/lib/reports/report-status-badge";

const defaultHref = (report: ReportListItemView) => `/reports/${report.id}`;

export function ReportsHistoryList({
  reports,
  hrefForReport = defaultHref,
}: {
  reports: ReportListItemView[];
  hrefForReport?: (report: ReportListItemView) => string;
}) {
  return (
    <ul className="flex flex-col divide-y divide-black/10 dark:divide-white/15">
      {reports.map((report) => (
        <li key={report.id}>
          <Link
            href={hrefForReport(report)}
            className="flex flex-wrap items-center justify-between gap-2 py-3 hover:underline"
          >
            <span className="flex flex-col">
              <span className="text-sm font-medium">
                {report.weekStart} – {report.weekEnd}
              </span>
              <span className="text-sm text-gray-500">{report.projectName}</span>
            </span>
            <ReportStatusBadge status={report.status} />
          </Link>
        </li>
      ))}
    </ul>
  );
}
