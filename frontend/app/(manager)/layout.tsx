import type { ReactNode } from "react";
import { RoleGuardedSection } from "@/lib/role-guarded-section";

export default function ManagerLayout({ children }: { children: ReactNode }) {
  return <RoleGuardedSection allowedRoles={["MANAGER"]}>{children}</RoleGuardedSection>;
}
