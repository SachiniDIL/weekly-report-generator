"use client";

import Link from "next/link";
import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { TextField } from "../text-field";
import { useForgotPasswordMutation } from "./use-forgot-password-mutation";
import { validateEmail } from "./validate-email";

export default function ForgotPasswordPage() {
  const mutation = useForgotPasswordMutation();
  const [email, setEmail] = useState("");
  const [error, setError] = useState<string>();

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value);
    setError(undefined);
    if (mutation.isError) {
      mutation.reset();
    }
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const validationError = validateEmail(email).email;
    setError(validationError);
    if (!validationError) {
      mutation.mutate(email.trim());
    }
  }

  if (mutation.isSuccess) {
    return (
      <div className="flex flex-col gap-4">
        <h1 className="text-xl font-semibold">Request submitted</h1>
        <p
          role="status"
          className="rounded bg-green-50 px-3 py-2 text-sm text-green-800 dark:bg-green-950 dark:text-green-300"
        >
          {mutation.data.message}
        </p>
        <Link href="/login" className="text-sm underline">
          Back to sign in
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
      <h1 className="text-xl font-semibold">Reset your password</h1>
      <p className="text-sm text-black/60 dark:text-white/60">
        Enter your email and we&apos;ll send a reset link if an account exists.
      </p>

      {mutation.isError ? (
        <p
          role="alert"
          className="rounded bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300"
        >
          {describeError(mutation.error)}
        </p>
      ) : null}

      <TextField
        id="email"
        label="Email"
        type="email"
        autoComplete="email"
        value={email}
        onChange={handleChange}
        error={error}
      />

      <button
        type="submit"
        disabled={mutation.isPending}
        className="rounded bg-foreground px-3 py-2 text-sm font-medium text-background disabled:opacity-60"
      >
        {mutation.isPending ? "Sending…" : "Send reset link"}
      </button>

      <Link href="/login" className="text-sm underline">
        Back to sign in
      </Link>
    </form>
  );
}
