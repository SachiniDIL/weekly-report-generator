import type { ReactNode } from "react";
import { AppShell } from "@/lib/app-shell/app-shell";
import { RoleGuardedSection } from "@/lib/role-guarded-section";
import { AiChatWidget } from "./ai-chat-widget";

export default function ManagerLayout({ children }: { children: ReactNode }) {
  return (
    <RoleGuardedSection allowedRoles={["MANAGER"]}>
      <AppShell variant="manager">{children}</AppShell>
      <AiChatWidget />
    </RoleGuardedSection>
  );
}
