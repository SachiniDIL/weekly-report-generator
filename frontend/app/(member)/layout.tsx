import type { ReactNode } from "react";
import { RoleGuardedSection } from "@/lib/role-guarded-section";

export default function MemberLayout({ children }: { children: ReactNode }) {
  return <RoleGuardedSection allowedRoles={["MEMBER"]}>{children}</RoleGuardedSection>;
}
