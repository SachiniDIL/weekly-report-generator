"use client";

import { X } from "lucide-react";
import { useToast, type ToastTone } from "./toast-context";

const TONE_CLASS: Record<ToastTone, string> = {
  success: "dusk-banner-success",
  error: "dusk-banner-error",
  info: "dusk-banner",
};

/**
 * Renders the active toasts, bottom-right, stacked newest-last. Mounted once, next to the
 * provider. Errors get `role="alert"` (assertive); the rest are polite status updates.
 */
export function ToastViewport() {
  const { toasts, dismiss } = useToast();

  if (toasts.length === 0) {
    return null;
  }

  return (
    <div
      className="pointer-events-none fixed right-0 top-0 z-50 flex w-full flex-col items-end gap-2 p-4 sm:max-w-sm"
      aria-live="polite"
    >
      {toasts.map((toast) => (
        <div
          key={toast.id}
          role={toast.tone === "error" ? "alert" : "status"}
          className={`dusk-toast pointer-events-auto flex w-full items-start gap-3 p-3 text-sm ${TONE_CLASS[toast.tone]}`}
        >
          <p className="flex-1">{toast.message}</p>
          <button
            type="button"
            onClick={() => dismiss(toast.id)}
            aria-label="Dismiss"
            className="shrink-0 opacity-60 transition-opacity hover:opacity-100"
          >
            <X size={14} aria-hidden />
          </button>
        </div>
      ))}
    </div>
  );
}
