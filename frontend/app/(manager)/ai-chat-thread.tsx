import { AiMarkdown } from "@/lib/ai/ai-markdown";
import type { ChatMessage } from "@/lib/ai/use-ai-chat";

export function AiChatThread({
  messages,
  isSending,
  error,
}: {
  messages: ChatMessage[];
  isSending: boolean;
  error: string | null;
}) {
  const isEmpty = messages.length === 0 && !isSending && error === null;

  return (
    <div
      className="flex-1 overflow-y-auto px-3 py-2 text-sm"
      aria-live="polite"
    >
      {isEmpty ? (
        <p className="text-dusk-secondary">
          Ask a question about your team&apos;s reports — for example,
          &ldquo;Who is blocked this week?&rdquo;
        </p>
      ) : null}

      <ul className="flex flex-col gap-2">
        {messages.map((message) =>
          message.role === "user" ? (
            <li
              key={message.id}
              className="max-w-[85%] self-end whitespace-pre-wrap rounded-lg bg-foreground px-2.5 py-1.5 text-background"
            >
              {message.text}
            </li>
          ) : (
            <li
              key={message.id}
              className="max-w-[85%] self-start rounded-lg border border-white/10 bg-white/[.04] px-2.5 py-1.5 text-dusk-primary"
            >
              <AiMarkdown text={message.text} />
            </li>
          ),
        )}
      </ul>

      {isSending ? (
        <div
          className="mt-2 flex flex-col gap-1.5"
          role="status"
          aria-label="Thinking"
        >
          <div className="dusk-skeleton h-3 w-24" />
          <div className="dusk-skeleton h-3 w-40" />
        </div>
      ) : null}
      {error ? (
        <p role="alert" className="mt-2 text-red-700">
          {error}
        </p>
      ) : null}
    </div>
  );
}
