"use client";

import Link from "next/link";
import { useState } from "react";
import { ApiError, describeError } from "@/lib/api-client";
import { TextField } from "../text-field";
import { useRegisterMutation } from "./use-register-mutation";
import {
  validateRegistration,
  type RegistrationErrors,
  type RegistrationForm,
} from "./validate-registration";

const EMPTY_FORM: RegistrationForm = { name: "", email: "", password: "", confirmPassword: "" };

export default function RegisterPage() {
  const mutation = useRegisterMutation();
  const [form, setForm] = useState<RegistrationForm>(EMPTY_FORM);
  const [clientErrors, setClientErrors] = useState<RegistrationErrors>({});

  const serverFieldErrors =
    mutation.error instanceof ApiError ? (mutation.error.fieldErrors ?? {}) : {};
  const hasServerFieldErrors = Object.keys(serverFieldErrors).length > 0;

  function fieldError(field: keyof RegistrationForm): string | undefined {
    return clientErrors[field] ?? serverFieldErrors[field];
  }

  function updateField(field: keyof RegistrationForm) {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      const { value } = event.target;
      setForm((current) => ({ ...current, [field]: value }));
      setClientErrors((current) => ({ ...current, [field]: undefined }));
      if (mutation.isError) {
        mutation.reset();
      }
    };
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validateRegistration(form);
    setClientErrors(nextErrors);
    if (Object.keys(nextErrors).length === 0) {
      mutation.mutate({ name: form.name, email: form.email, password: form.password });
    }
  }

  if (mutation.isSuccess) {
    return (
      <div className="flex flex-col gap-4">
        <h1 className="text-xl font-semibold">Registration submitted</h1>
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
      <h1 className="text-xl font-semibold">Create an account</h1>

      {mutation.isError && !hasServerFieldErrors ? (
        <p
          role="alert"
          className="rounded bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300"
        >
          {describeError(mutation.error)}
        </p>
      ) : null}

      <TextField
        id="name"
        label="Name"
        autoComplete="name"
        value={form.name}
        onChange={updateField("name")}
        error={fieldError("name")}
      />
      <TextField
        id="email"
        label="Email"
        type="email"
        autoComplete="email"
        value={form.email}
        onChange={updateField("email")}
        error={fieldError("email")}
      />
      <TextField
        id="password"
        label="Password"
        type="password"
        autoComplete="new-password"
        value={form.password}
        onChange={updateField("password")}
        error={fieldError("password")}
      />
      <TextField
        id="confirmPassword"
        label="Confirm password"
        type="password"
        autoComplete="new-password"
        value={form.confirmPassword}
        onChange={updateField("confirmPassword")}
        error={fieldError("confirmPassword")}
      />

      <button
        type="submit"
        disabled={mutation.isPending}
        className="rounded bg-foreground px-3 py-2 text-sm font-medium text-background disabled:opacity-60"
      >
        {mutation.isPending ? "Submitting…" : "Create account"}
      </button>

      <Link href="/login" className="text-sm underline">
        Already have an account? Sign in
      </Link>
    </form>
  );
}
