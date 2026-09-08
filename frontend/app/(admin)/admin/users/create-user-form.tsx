"use client";

import { useState } from "react";
import type { Role } from "@/lib/api-client";
import { useConfirm } from "@/lib/confirm-dialog";
import { useCreateAdminUserMutation } from "./use-admin-user-mutations";

const ALL_ROLES: Role[] = ["ADMIN", "MANAGER", "MEMBER"];
const EMPTY_FORM = {
  name: "",
  email: "",
  password: "",
  role: "MEMBER" as Role,
};

export function CreateUserForm() {
  const [form, setForm] = useState(EMPTY_FORM);
  const [validationError, setValidationError] = useState<string | null>(null);
  const mutation = useCreateAdminUserMutation();
  const confirm = useConfirm();

  function update<Key extends keyof typeof form>(
    key: Key,
    value: (typeof form)[Key],
  ) {
    setForm((current) => ({ ...current, [key]: value }));
    setValidationError(null);
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (
      form.name.trim() === "" ||
      form.email.trim() === "" ||
      form.password.length < 8
    ) {
      setValidationError(
        "Name, email, and a password of at least 8 characters are required.",
      );
      return;
    }

    const confirmed = await confirm({
      title: "Create this user?",
      message: `${form.email.trim()} will get an active ${form.role} account and can sign in immediately.`,
      confirmLabel: "Create user",
    });
    if (!confirmed) {
      return;
    }

    mutation.mutate(
      {
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
        role: form.role,
      },
      { onSuccess: () => setForm(EMPTY_FORM) },
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-3 rounded border border-black/15 p-4 dark:border-white/20"
    >
      <h2 className="text-sm font-semibold uppercase tracking-wide text-gray-500">
        Create a user directly
      </h2>

      <LabeledInput
        id="new-user-name"
        label="Name"
        value={form.name}
        onChange={(value) => update("name", value)}
      />
      <LabeledInput
        id="new-user-email"
        label="Email"
        type="email"
        value={form.email}
        onChange={(value) => update("email", value)}
      />
      <LabeledInput
        id="new-user-password"
        label="Password"
        type="password"
        value={form.password}
        onChange={(value) => update("password", value)}
      />

      <div className="flex flex-col gap-1">
        <label htmlFor="new-user-role" className="text-sm font-medium">
          Role
        </label>
        <select
          id="new-user-role"
          value={form.role}
          onChange={(event) => update("role", event.target.value as Role)}
          className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20"
        >
          {ALL_ROLES.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      </div>

      {validationError ? (
        <p className="text-sm text-red-600 dark:text-red-400">
          {validationError}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={mutation.isPending}
        className="self-start rounded bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-60"
      >
        {mutation.isPending ? "Creating…" : "Create user"}
      </button>
    </form>
  );
}

function LabeledInput({
  id,
  label,
  type = "text",
  value,
  onChange,
}: {
  id: string;
  label: string;
  type?: string;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={id} className="text-sm font-medium">
        {label}
      </label>
      <input
        id={id}
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm outline-none focus:border-black/40 dark:border-white/20"
      />
    </div>
  );
}
