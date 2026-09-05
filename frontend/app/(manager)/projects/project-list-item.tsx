"use client";

import { useState } from "react";
import { describeError } from "@/lib/api-client";
import type { ProjectResponse } from "@/lib/api/projects";
import { ProjectForm } from "./project-form";
import { ProjectMembersPanel } from "./project-members-panel";
import { useArchiveProjectMutation } from "./use-project-mutations";

export function ProjectListItem({ project }: { project: ProjectResponse }) {
  const [isEditing, setIsEditing] = useState(false);
  const [showMembers, setShowMembers] = useState(false);
  const archive = useArchiveProjectMutation();

  if (isEditing) {
    return (
      <li className="py-3">
        <ProjectForm project={project} onDone={() => setIsEditing(false)} />
      </li>
    );
  }

  return (
    <li className="flex flex-col gap-2 py-3">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <p className="text-sm font-medium">
            {project.name}
            {!project.active ? (
              <span className="ml-2 rounded bg-gray-100 px-2 py-0.5 text-xs text-gray-600 dark:bg-gray-800 dark:text-gray-300">
                Archived
              </span>
            ) : null}
          </p>
          {project.description ? (
            <p className="text-sm text-gray-500">{project.description}</p>
          ) : null}
        </div>

        <div className="flex gap-3 text-sm">
          <button type="button" onClick={() => setShowMembers((open) => !open)} className="underline">
            {showMembers ? "Hide members" : "Members"}
          </button>
          <button type="button" onClick={() => setIsEditing(true)} className="underline">
            Edit
          </button>
          {project.active ? (
            <button
              type="button"
              onClick={() => archive.mutate(project.id)}
              disabled={archive.isPending}
              className="text-red-600 underline disabled:opacity-50"
            >
              Archive
            </button>
          ) : null}
        </div>
      </div>

      {archive.isError ? (
        <p role="alert" className="text-sm text-red-700 dark:text-red-300">
          {describeError(archive.error)}
        </p>
      ) : null}

      {showMembers ? <ProjectMembersPanel projectId={project.id} /> : null}
    </li>
  );
}
