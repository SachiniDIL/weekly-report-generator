import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { render } from "@testing-library/react";
import type { ReactElement, ReactNode } from "react";
import { ConfirmProvider } from "@/lib/confirm-dialog";
import { ToastProvider } from "@/lib/toast/toast-context";

/**
 * Renders a component under a fresh QueryClient (retries off), plus the toast and confirm
 * providers so components that fire toasts or gate an action behind `useConfirm()` behave as
 * they do in the app.
 */
export function renderWithQueryClient(ui: ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <ConfirmProvider>{children}</ConfirmProvider>
        </ToastProvider>
      </QueryClientProvider>
    );
  }

  return render(ui, { wrapper: Wrapper });
}
