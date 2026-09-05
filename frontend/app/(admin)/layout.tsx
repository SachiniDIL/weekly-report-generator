import type { ReactNode } from "react";
import { RoleGuardedSection } from "@/lib/role-guarded-section";

export default function AdminLayout({ children }: { children: ReactNode }) {
  return <RoleGuardedSection allowedRoles={["ADMIN"]}>{children}</RoleGuardedSection>;
}
