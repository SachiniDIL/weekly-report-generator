"use client";

import { useState } from "react";
import { useAiChat } from "@/lib/ai/use-ai-chat";
import { AiChatComposer } from "./ai-chat-composer";
import { AiChatThread } from "./ai-chat-thread";

export function AiChatWidget() {
  const [open, setOpen] = useState(false);
  const chat = useAiChat();

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col items-end gap-2">
      {open ? (
        <section
          aria-label="AI assistant"
          className="flex h-[28rem] w-80 max-w-[calc(100vw-2rem)] flex-col overflow-hidden rounded-xl border border-white/15 bg-dusk-base"
          style={{
            boxShadow:
              "12px 12px 32px rgba(57, 65, 90, 0.35), -8px -8px 22px #ffffff",
          }}
        >
          <header className="flex items-center justify-between border-b border-white/10 px-3 py-2">
            <h2 className="text-sm font-semibold text-dusk-primary">
              Ask about your team
            </h2>
            <button
              type="button"
              onClick={() => setOpen(false)}
              aria-label="Close assistant"
              className="text-dusk-secondary hover:text-dusk-accent-light"
            >
              ✕
            </button>
          </header>

          <AiChatThread
            messages={chat.messages}
            isSending={chat.isSending}
            error={chat.error}
          />
          <AiChatComposer onSend={chat.send} disabled={chat.isSending} />
        </section>
      ) : null}

      <button
        type="button"
        onClick={() => setOpen((current) => !current)}
        aria-expanded={open}
        className="rounded-full bg-foreground px-4 py-3 text-sm font-medium text-background"
        style={{
          boxShadow:
            "6px 6px 16px rgba(91, 99, 230, 0.4), -6px -6px 16px #ffffff",
        }}
      >
        {open ? "Hide assistant" : "Ask AI"}
      </button>
    </div>
  );
}
