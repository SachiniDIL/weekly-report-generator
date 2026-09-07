"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import { SkeletonText } from "@/lib/skeleton";
import { useProjectsQuery } from "@/lib/projects/use-projects-query";
import { ProjectForm } from "./project-form";
import { ProjectListItem } from "./project-list-item";

export default function ProjectsPage() {
  const [showArchived, setShowArchived] = useState(false);
  const [creating, setCreating] = useState(false);
  const projects = useProjectsQuery(showArchived);

  return (
    <main className="mx-auto flex max-w-3xl flex-col gap-6 p-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-xl font-semibold text-dusk-primary">Projects</h1>
        <button
          type="button"
          onClick={() => setCreating((open) => !open)}
          className="rounded-lg bg-foreground px-4 py-2 text-sm font-medium text-background"
        >
          {creating ? "Close" : "New project"}
        </button>
      </header>

      {creating ? <ProjectForm onDone={() => setCreating(false)} /> : null}

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={showArchived}
          onChange={(event) => setShowArchived(event.target.checked)}
        />
        Show archived projects
      </label>

      {projects.isPending ? <SkeletonText lines={4} className="mt-1" /> : null}
      {projects.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(projects.error)}
        </p>
      ) : null}
      {projects.isSuccess && projects.data.length === 0 ? (
        <p className="text-sm text-gray-500">No projects yet.</p>
      ) : null}
      {projects.isSuccess && projects.data.length > 0 ? (
        <ul className="flex flex-col divide-y divide-black/10 dark:divide-white/15">
          {projects.data.map((project) => (
            <ProjectListItem key={project.id} project={project} />
          ))}
        </ul>
      ) : null}
    </main>
  );
}
