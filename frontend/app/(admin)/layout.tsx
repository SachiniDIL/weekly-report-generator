import type { ReactNode } from "react";
import { AppShell } from "@/lib/app-shell/app-shell";
import { RoleGuardedSection } from "@/lib/role-guarded-section";

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <RoleGuardedSection allowedRoles={["ADMIN"]}>
      <AppShell variant="admin">{children}</AppShell>
    </RoleGuardedSection>
  );
}
