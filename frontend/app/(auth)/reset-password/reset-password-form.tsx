"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { TextField } from "../text-field";
import { useResetPasswordMutation } from "./use-reset-password-mutation";
import {
  validateNewPassword,
  type PasswordResetErrors,
  type PasswordResetFields,
} from "./validate-new-password";

const EMPTY_FIELDS: PasswordResetFields = { newPassword: "", confirmPassword: "" };

export function ResetPasswordForm() {
  const token = useSearchParams().get("token");

  if (!token) {
    return (
      <div className="flex flex-col gap-4">
        <h1 className="text-xl font-semibold">Invalid reset link</h1>
        <p className="text-sm text-black/60 dark:text-white/60">
          Open the link from your password reset email, or request a new one.
        </p>
        <Link href="/forgot-password" className="text-sm underline">
          Request a new link
        </Link>
      </div>
    );
  }

  return <NewPasswordForm token={token} />;
}

function NewPasswordForm({ token }: { token: string }) {
  const mutation = useResetPasswordMutation();
  const [fields, setFields] = useState<PasswordResetFields>(EMPTY_FIELDS);
  const [errors, setErrors] = useState<PasswordResetErrors>({});

  function updateField(field: keyof PasswordResetFields) {
    return (event: React.ChangeEvent<HTMLInputElement>) => {
      const { value } = event.target;
      setFields((current) => ({ ...current, [field]: value }));
      setErrors((current) => ({ ...current, [field]: undefined }));
      if (mutation.isError) {
        mutation.reset();
      }
    };
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors = validateNewPassword(fields);
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length === 0) {
      mutation.mutate({ token, newPassword: fields.newPassword });
    }
  }

  if (mutation.isSuccess) {
    return (
      <div className="flex flex-col gap-4">
        <h1 className="text-xl font-semibold">Password reset</h1>
        <p
          role="status"
          className="rounded bg-green-50 px-3 py-2 text-sm text-green-800 dark:bg-green-950 dark:text-green-300"
        >
          {mutation.data.message}
        </p>
        <Link href="/login" className="text-sm underline">
          Sign in
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="flex flex-col gap-4">
      <h1 className="text-xl font-semibold">Choose a new password</h1>

      {mutation.isError ? (
        <p
          role="alert"
          className="rounded bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300"
        >
          {describeError(mutation.error)}
        </p>
      ) : null}

      <TextField
        id="newPassword"
        label="New password"
        type="password"
        autoComplete="new-password"
        value={fields.newPassword}
        onChange={updateField("newPassword")}
        error={errors.newPassword}
      />
      <TextField
        id="confirmPassword"
        label="Confirm password"
        type="password"
        autoComplete="new-password"
        value={fields.confirmPassword}
        onChange={updateField("confirmPassword")}
        error={errors.confirmPassword}
      />

      <button
        type="submit"
        disabled={mutation.isPending}
        className="rounded bg-foreground px-3 py-2 text-sm font-medium text-background disabled:opacity-60"
      >
        {mutation.isPending ? "Resetting…" : "Reset password"}
      </button>

      <Link href="/login" className="text-sm underline">
        Back to sign in
      </Link>
    </form>
  );
}
