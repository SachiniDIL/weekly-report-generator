"use client";

import { useState } from "react";

export function AiChatComposer({
  onSend,
  disabled,
}: {
  onSend: (question: string) => void;
  disabled: boolean;
}) {
  const [value, setValue] = useState("");
  const canSend = !disabled && value.trim() !== "";

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!canSend) {
      return;
    }
    onSend(value);
    setValue("");
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="flex gap-2 border-t border-white/10 p-2"
    >
      <input
        type="text"
        value={value}
        onChange={(event) => setValue(event.target.value)}
        disabled={disabled}
        aria-label="Your question"
        placeholder="Ask a question…"
        className="min-w-0 flex-1 px-2 py-1 text-sm disabled:opacity-60"
      />
      <button
        type="submit"
        disabled={!canSend}
        className="rounded-lg bg-foreground px-3 py-1 text-sm font-medium text-background disabled:opacity-60"
      >
        Send
      </button>
    </form>
  );
}
