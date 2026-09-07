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
    <div className="flex-1 overflow-y-auto px-3 py-2 text-sm" aria-live="polite">
      {isEmpty ? (
        <p className="text-gray-500">
          Ask a question about your team&apos;s reports — for example, &ldquo;Who is blocked this
          week?&rdquo;
        </p>
      ) : null}

      <ul className="flex flex-col gap-2">
        {messages.map((message) => (
          <li
            key={message.id}
            className={
              message.role === "user"
                ? "self-end rounded bg-foreground px-2 py-1 text-background"
                : "self-start rounded bg-black/[.05] px-2 py-1 whitespace-pre-wrap dark:bg-white/[.08]"
            }
          >
            {message.text}
          </li>
        ))}
      </ul>

      {isSending ? <p className="mt-2 text-gray-500">Thinking…</p> : null}
      {error ? (
        <p role="alert" className="mt-2 text-red-700 dark:text-red-300">
          {error}
        </p>
      ) : null}
    </div>
  );
}
