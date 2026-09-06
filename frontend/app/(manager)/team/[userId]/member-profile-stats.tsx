import type { MemberProfile } from "@/lib/api/dashboard";

export function MemberProfileStats({ profile }: { profile: MemberProfile }) {
  return (
    <section className="flex flex-col gap-4">
      <header>
        <h1 className="text-xl font-semibold">{profile.name}</h1>
        <p className="text-sm text-gray-500">{profile.email}</p>
      </header>

      <div className="grid gap-3 sm:grid-cols-2">
        <StatCard label="Reports submitted" value={profile.totalReportsSubmitted} />
        <StatCard label="Reports needing correction" value={profile.needsCorrectionCount} />
      </div>

      <HoursByTaskType hours={profile.hoursByTaskType} />
    </section>
  );
}

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded border border-black/10 p-4 dark:border-white/15">
      <p className="text-xs uppercase tracking-wide text-gray-500">{label}</p>
      <p className="mt-1 text-2xl font-semibold">{value}</p>
    </div>
  );
}

function HoursByTaskType({ hours }: { hours: MemberProfile["hoursByTaskType"] }) {
  return (
    <div className="rounded border border-black/10 p-4 dark:border-white/15">
      <p className="text-xs uppercase tracking-wide text-gray-500">Hours by task type</p>
      {hours.length === 0 ? (
        <p className="mt-2 text-sm text-gray-500">No hours logged yet.</p>
      ) : (
        <dl className="mt-2 flex flex-col divide-y divide-black/5 text-sm dark:divide-white/10">
          {hours.map((point) => (
            <div key={point.taskType} className="flex justify-between py-1.5">
              <dt>{point.taskType}</dt>
              <dd className="font-medium">{point.totalHours}h</dd>
            </div>
          ))}
        </dl>
      )}
    </div>
  );
}
