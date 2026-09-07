"use client";

import { useParams } from "next/navigation";
import { ApiError, describeError } from "@/lib/api-client";
import { BackLink } from "@/lib/back-link";
import { useMemberProfileQuery } from "@/lib/dashboard/dashboard-queries";
import { ReportMessage } from "@/lib/reports/report-message";
import { MemberProfileStats } from "./member-profile-stats";
import { MemberReportHistory } from "./member-report-history";

export default function TeamMemberProfilePage() {
  const params = useParams<{ userId: string }>();
  const userId = Number(params.userId);
  const profile = useMemberProfileQuery(userId);

  const isUnknownMember =
    !Number.isFinite(userId) ||
    (profile.isError &&
      profile.error instanceof ApiError &&
      profile.error.status === 404);

  if (isUnknownMember) {
    return (
      <ReportMessage>
        This team member was not found. They may have been removed, or the id is
        not a team member.
      </ReportMessage>
    );
  }
  if (profile.isError) {
    return (
      <ReportMessage tone="error">{describeError(profile.error)}</ReportMessage>
    );
  }
  if (profile.isPending) {
    return <ReportMessage tone="loading" />;
  }

  return (
    <main className="mx-auto flex w-full max-w-3xl flex-col gap-6">
      <BackLink href="/team" label="Team" />
      <MemberProfileStats profile={profile.data} />
      <MemberReportHistory userId={userId} />
    </main>
  );
}
