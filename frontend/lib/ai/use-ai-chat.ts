"use client";

import { useMutation } from "@tanstack/react-query";
import { useRef, useState } from "react";
import { describeError } from "@/lib/api-client";
import { sendChatMessage } from "@/lib/api/ai";

export interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  text: string;
}

/**
 * Session-only chat state for the assistant widget: the running thread plus the send action.
 * Nothing is persisted — the thread lives in React state and is gone when the tab closes.
 */
export function useAiChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const nextId = useRef(0);

  const append = (role: ChatMessage["role"], text: string) => {
    setMessages((current) => [...current, { id: `m${nextId.current++}`, role, text }]);
  };

  const mutation = useMutation({
    mutationFn: (question: string) => sendChatMessage({ question }),
    onSuccess: (response) => append("assistant", response.message),
  });

  function send(question: string) {
    const trimmed = question.trim();
    if (trimmed === "" || mutation.isPending) {
      return;
    }
    append("user", trimmed);
    mutation.mutate(trimmed);
  }

  return {
    messages,
    send,
    isSending: mutation.isPending,
    error: mutation.isError ? describeError(mutation.error) : null,
  };
}
