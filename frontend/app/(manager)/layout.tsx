import type { ReactNode } from "react";
import { RoleGuardedSection } from "@/lib/role-guarded-section";
import { AiChatWidget } from "./ai-chat-widget";

export default function ManagerLayout({ children }: { children: ReactNode }) {
  return (
    <RoleGuardedSection allowedRoles={["MANAGER"]}>
      {children}
      <AiChatWidget />
    </RoleGuardedSection>
  );
}
