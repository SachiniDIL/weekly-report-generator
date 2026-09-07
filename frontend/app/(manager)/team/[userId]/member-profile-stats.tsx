import { Avatar } from "@/lib/avatar";
import type { MemberProfile } from "@/lib/api/dashboard";

export function MemberProfileStats({ profile }: { profile: MemberProfile }) {
  return (
    <section className="flex flex-col gap-4">
      <header className="flex items-center gap-3">
        <Avatar name={profile.name} size={44} />
        <div>
          <h1 className="text-xl font-semibold text-dusk-primary">
            {profile.name}
          </h1>
          <p className="text-sm text-dusk-secondary">{profile.email}</p>
        </div>
      </header>

      <div className="grid gap-3 sm:grid-cols-2">
        <StatCard
          label="Reports submitted"
          value={profile.totalReportsSubmitted}
        />
        <StatCard
          label="Reports needing correction"
          value={profile.needsCorrectionCount}
        />
      </div>

      <HoursByTaskType hours={profile.hoursByTaskType} />
    </section>
  );
}

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="dusk-panel p-4">
      <p className="text-xs uppercase tracking-wide text-dusk-secondary">
        {label}
      </p>
      <p className="mt-1 text-2xl font-semibold text-dusk-primary">{value}</p>
    </div>
  );
}

function HoursByTaskType({
  hours,
}: {
  hours: MemberProfile["hoursByTaskType"];
}) {
  return (
    <div className="dusk-panel p-4">
      <p className="text-xs uppercase tracking-wide text-dusk-secondary">
        Hours by task type
      </p>
      {hours.length === 0 ? (
        <p className="mt-2 text-sm text-dusk-secondary">No hours logged yet.</p>
      ) : (
        <dl className="mt-2 flex flex-col divide-y divide-black/5 text-sm">
          {hours.map((point) => (
            <div key={point.taskType} className="flex justify-between py-1.5">
              <dt className="text-dusk-secondary">{point.taskType}</dt>
              <dd className="font-medium text-dusk-primary">
                {point.totalHours}h
              </dd>
            </div>
          ))}
        </dl>
      )}
    </div>
  );
}
