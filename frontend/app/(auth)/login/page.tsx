"use client";

import Link from "next/link";
import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { TextField } from "../text-field";
import { useLoginMutation } from "./use-login-mutation";
import {
  validateCredentials,
  type CredentialErrors,
  type Credentials,
} from "./validate-credentials";

export default function LoginPage() {
  const mutation = useLoginMutation();
  const [form, setForm] = useState<Credentials>({ email: "", password: "" });
  const [errors, setErrors] = useState<CredentialErrors>({});

  function updateField(field: keyof Credentials) {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      const { value } = event.target;
      setForm((current) => ({ ...current, [field]: value }));
      setErrors((current) => ({ ...current, [field]: undefined }));
      if (mutation.isError) {
        mutation.reset();
      }
    };
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validateCredentials(form);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length === 0) {
      mutation.mutate(form);
    }
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
      <h1 className="text-xl font-semibold">Sign in</h1>

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
        value={form.email}
        onChange={updateField("email")}
        error={errors.email}
      />
      <TextField
        id="password"
        label="Password"
        type="password"
        autoComplete="current-password"
        value={form.password}
        onChange={updateField("password")}
        error={errors.password}
      />

      <button
        type="submit"
        disabled={mutation.isPending}
        className="rounded bg-foreground px-3 py-2 text-sm font-medium text-background disabled:opacity-60"
      >
        {mutation.isPending ? "Signing in…" : "Sign in"}
      </button>

      <div className="flex justify-between text-sm">
        <Link href="/register" className="underline">
          Create an account
        </Link>
        <Link href="/forgot-password" className="underline">
          Forgot password?
        </Link>
      </div>
    </form>
  );
}
