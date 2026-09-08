"use client";

import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";

export type ToastTone = "success" | "error" | "info";

export interface Toast {
  id: number;
  tone: ToastTone;
  message: string;
}

export interface ToastApi {
  toasts: Toast[];
  /** Show a toast; returns its id. */
  show: (tone: ToastTone, message: string) => number;
  success: (message: string) => number;
  error: (message: string) => number;
  info: (message: string) => number;
  dismiss: (id: number) => void;
}

const AUTO_DISMISS_MS: Record<ToastTone, number> = {
  success: 4000,
  info: 4000,
  // Errors linger — the user may have looked away when the request failed.
  error: 7000,
};

/** At most this many stacked at once; the oldest is dropped when a new one arrives. */
const MAX_VISIBLE = 4;

/**
 * A no-op API so `useToast()` is safe to call outside a provider (e.g. in unit tests that render
 * a component in isolation). The real provider replaces this.
 */
const NO_OP: ToastApi = {
  toasts: [],
  show: () => -1,
  success: () => -1,
  error: () => -1,
  info: () => -1,
  dismiss: () => {},
};

const ToastContext = createContext<ToastApi>(NO_OP);

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const nextId = useRef(1);
  const timers = useRef(new Map<number, ReturnType<typeof setTimeout>>());

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id));
    const timer = timers.current.get(id);
    if (timer) {
      clearTimeout(timer);
      timers.current.delete(id);
    }
  }, []);

  const show = useCallback(
    (tone: ToastTone, message: string) => {
      const id = nextId.current++;
      setToasts((current) =>
        [...current, { id, tone, message }].slice(-MAX_VISIBLE),
      );
      const timer = setTimeout(() => dismiss(id), AUTO_DISMISS_MS[tone]);
      timers.current.set(id, timer);
      return id;
    },
    [dismiss],
  );

  const api = useMemo<ToastApi>(
    () => ({
      toasts,
      show,
      success: (message) => show("success", message),
      error: (message) => show("error", message),
      info: (message) => show("info", message),
      dismiss,
    }),
    [toasts, show, dismiss],
  );

  return <ToastContext.Provider value={api}>{children}</ToastContext.Provider>;
}

export function useToast(): ToastApi {
  return useContext(ToastContext);
}
