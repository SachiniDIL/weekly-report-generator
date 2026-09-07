import type { ReactNode } from "react";
import { AppShell } from "@/lib/app-shell/app-shell";
import { RoleGuardedSection } from "@/lib/role-guarded-section";

export default function MemberLayout({ children }: { children: ReactNode }) {
  return (
    <RoleGuardedSection allowedRoles={["MEMBER"]}>
      <AppShell variant="member">{children}</AppShell>
    </RoleGuardedSection>
  );
}
