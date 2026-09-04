import { Suspense } from "react";
import { ResetPasswordForm } from "./reset-password-form";

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={<p className="text-sm text-black/60 dark:text-white/60">Loading…</p>}>
      <ResetPasswordForm />
    </Suspense>
  );
}
