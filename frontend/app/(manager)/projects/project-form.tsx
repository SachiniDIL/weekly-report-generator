"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import type { ProjectResponse } from "@/lib/api/projects";
import { useSaveProjectMutation } from "./use-project-mutations";

/** Create a project (no `project`) or edit an existing one. */
export function ProjectForm({
  project,
  onDone,
}: {
  project?: ProjectResponse;
  onDone: () => void;
}) {
  const [name, setName] = useState(project?.name ?? "");
  const [description, setDescription] = useState(project?.description ?? "");
  const [nameError, setNameError] = useState<string | null>(null);
  const mutation = useSaveProjectMutation(project?.id);

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (name.trim() === "") {
      setNameError("Name is required");
      return;
    }
    setNameError(null);
    mutation.mutate(
      { name: name.trim(), description: description.trim() === "" ? null : description.trim() },
      { onSuccess: onDone },
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-3 rounded border border-black/15 p-4 dark:border-white/20"
    >
      <div className="flex flex-col gap-1">
        <label htmlFor="project-name" className="text-sm font-medium">
          Name
        </label>
        <input
          id="project-name"
          value={name}
          onChange={(event) => {
            setName(event.target.value);
            setNameError(null);
          }}
          className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm outline-none focus:border-black/40 dark:border-white/20"
        />
        {nameError ? <p className="text-sm text-red-600 dark:text-red-400">{nameError}</p> : null}
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="project-description" className="text-sm font-medium">
          Description
        </label>
        <textarea
          id="project-description"
          rows={2}
          value={description}
          onChange={(event) => setDescription(event.target.value)}
          className="rounded border border-black/15 bg-transparent px-3 py-2 text-sm outline-none focus:border-black/40 dark:border-white/20"
        />
      </div>

      {mutation.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(mutation.error)}
        </p>
      ) : null}

      <div className="flex items-center gap-3">
        <button
          type="submit"
          disabled={mutation.isPending}
          className="rounded bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-60"
        >
          {project == null ? "Create project" : "Save changes"}
        </button>
        <button type="button" onClick={onDone} className="text-sm underline">
          Cancel
        </button>
      </div>
    </form>
  );
}
