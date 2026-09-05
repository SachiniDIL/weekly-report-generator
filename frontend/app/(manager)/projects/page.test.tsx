import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { ProjectMemberView, ProjectResponse } from "@/lib/api/projects";
import type { UserBasicView } from "@/lib/api/users";
import { renderWithQueryClient } from "@/lib/test-render";
import ProjectsPage from "./page";

function json(status: number, body: unknown): Response {
  return { ok: status < 300, status, json: async () => body } as unknown as Response;
}

function fakeBackend(seedMembers: Record<number, ProjectMemberView[]> = {}) {
  let nextId = 100;
  const projects: ProjectResponse[] = [
    { id: 1, name: "Apollo", description: "Space programme", active: true },
    { id: 2, name: "Zephyr", description: null, active: false },
  ];
  const members: Record<number, ProjectMemberView[]> = { ...seedMembers };
  const users: UserBasicView[] = [
    { id: 10, name: "Lin", role: "MEMBER" },
    { id: 11, name: "Dana", role: "MANAGER" },
    { id: 12, name: "Root", role: "ADMIN" },
  ];

  return jest.fn(async (rawUrl: string, init: RequestInit) => {
    const url = new URL(rawUrl);
    const path = url.pathname;
    const method = init.method ?? "GET";
    const body = init.body ? JSON.parse(init.body as string) : undefined;

    if (path === "/projects" && method === "GET") {
      const includeInactive = url.searchParams.get("includeInactive") === "true";
      return json(200, includeInactive ? projects : projects.filter((project) => project.active));
    }
    if (path === "/projects" && method === "POST") {
      const created = { id: nextId++, name: body.name, description: body.description ?? null, active: true };
      projects.push(created);
      return json(201, created);
    }
    if (path === "/users" && method === "GET") {
      return json(200, users);
    }

    const project = path.match(/^\/projects\/(\d+)$/);
    if (project) {
      const target = projects.find((candidate) => candidate.id === Number(project[1]))!;
      if (method === "PUT") {
        target.name = body.name;
        target.description = body.description ?? null;
        return json(200, target);
      }
      if (method === "DELETE") {
        if (!target.active) {
          return json(409, { message: `Project ${target.id} is already archived` });
        }
        target.active = false;
        return json(204, null);
      }
    }

    const memberList = path.match(/^\/projects\/(\d+)\/members$/);
    if (memberList && method === "GET") {
      return json(200, members[Number(memberList[1])] ?? []);
    }

    const member = path.match(/^\/projects\/(\d+)\/members\/(\d+)$/);
    if (member) {
      const projectId = Number(member[1]);
      const userId = Number(member[2]);
      if (method === "POST") {
        const user = users.find((candidate) => candidate.id === userId)!;
        const view = { userId, name: user.name, email: `${user.name.toLowerCase()}@example.com` };
        members[projectId] = [...(members[projectId] ?? []), view];
        return json(201, view);
      }
      if (method === "DELETE") {
        members[projectId] = (members[projectId] ?? []).filter((row) => row.userId !== userId);
        return json(204, null);
      }
    }

    throw new Error(`unhandled ${method} ${path}`);
  });
}

function useBackend(fetchImpl: jest.Mock) {
  global.fetch = fetchImpl as unknown as typeof fetch;
  setAuthToken(null);
}

async function openApollo() {
  return (await screen.findByText("Apollo")).closest("li") as HTMLElement;
}

describe("ProjectsPage", () => {
  it("creates a project and shows it in the list", async () => {
    useBackend(fakeBackend());
    renderWithQueryClient(<ProjectsPage />);
    await screen.findByText("Apollo");

    fireEvent.click(screen.getByRole("button", { name: "New project" }));
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "Helios" } });
    fireEvent.click(screen.getByRole("button", { name: "Create project" }));

    expect(await screen.findByText("Helios")).toBeInTheDocument();
  });

  it("archives an active project and no longer offers Archive on an archived one", async () => {
    useBackend(fakeBackend());
    renderWithQueryClient(<ProjectsPage />);
    const apollo = await openApollo();

    fireEvent.click(within(apollo).getByRole("button", { name: "Archive" }));
    await waitFor(() => expect(screen.queryByText("Apollo")).not.toBeInTheDocument());

    fireEvent.click(screen.getByRole("checkbox", { name: /show archived projects/i }));
    const archivedApollo = (await screen.findByText("Apollo")).closest("li") as HTMLElement;
    expect(within(archivedApollo).getByText("Archived")).toBeInTheDocument();
    expect(within(archivedApollo).queryByRole("button", { name: "Archive" })).not.toBeInTheDocument();
  });

  it("assigns a member, updates the member list, and never offers an admin", async () => {
    useBackend(fakeBackend());
    renderWithQueryClient(<ProjectsPage />);
    const apollo = await openApollo();

    fireEvent.click(within(apollo).getByRole("button", { name: "Members" }));
    expect(await within(apollo).findByText("No members assigned.")).toBeInTheDocument();
    expect(within(apollo).queryByRole("option", { name: /Root/ })).not.toBeInTheDocument();

    fireEvent.change(within(apollo).getByLabelText("Assign a member"), { target: { value: "10" } });
    fireEvent.click(within(apollo).getByRole("button", { name: "Add" }));

    expect(await within(apollo).findByText(/lin@example\.com/i)).toBeInTheDocument();
  });

  it("unassigns a member", async () => {
    useBackend(
      fakeBackend({ 1: [{ userId: 10, name: "Lin", email: "lin@example.com" }] }),
    );
    renderWithQueryClient(<ProjectsPage />);
    const apollo = await openApollo();

    fireEvent.click(within(apollo).getByRole("button", { name: "Members" }));
    expect(await within(apollo).findByText(/lin@example\.com/i)).toBeInTheDocument();

    fireEvent.click(within(apollo).getByRole("button", { name: "Remove" }));

    await waitFor(() =>
      expect(within(apollo).queryByText(/lin@example\.com/i)).not.toBeInTheDocument(),
    );
  });

  it("shows and hides archived projects via the toggle", async () => {
    useBackend(fakeBackend());
    renderWithQueryClient(<ProjectsPage />);
    await screen.findByText("Apollo");
    expect(screen.queryByText("Zephyr")).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("checkbox", { name: /show archived projects/i }));
    expect(await screen.findByText("Zephyr")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("checkbox", { name: /show archived projects/i }));
    await waitFor(() => expect(screen.queryByText("Zephyr")).not.toBeInTheDocument());
  });
});
