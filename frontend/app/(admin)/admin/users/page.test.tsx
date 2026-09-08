import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { AdminUserView } from "@/lib/api/admin-users";
import { renderWithQueryClient } from "@/lib/test-render";
import AdminUsersPage from "./page";

function json(status: number, body: unknown): Response {
  return {
    ok: status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

function fakeBackend() {
  let nextId = 100;
  const users: AdminUserView[] = [
    {
      id: 1,
      name: "Percy Pending",
      email: "percy@example.com",
      role: null,
      status: "PENDING",
      createdAt: "2026-09-01T00:00:00Z",
    },
    {
      id: 2,
      name: "Ada Active",
      email: "ada@example.com",
      role: "MEMBER",
      status: "ACTIVE",
      createdAt: "2026-09-02T00:00:00Z",
    },
  ];

  return jest.fn(async (rawUrl: string, init: RequestInit) => {
    const { pathname } = new URL(rawUrl);
    const method = init.method ?? "GET";
    const body = init.body ? JSON.parse(init.body as string) : undefined;

    if (pathname === "/admin/users" && method === "GET") {
      // Fresh copies each call so React Query's structural sharing sees real changes.
      return json(
        200,
        users.map((user) => ({ ...user })),
      );
    }
    if (pathname === "/admin/users" && method === "POST") {
      const created: AdminUserView = {
        id: nextId++,
        name: body.name,
        email: body.email,
        role: body.role,
        status: "ACTIVE",
        createdAt: "2026-09-10T00:00:00Z",
      };
      users.push(created);
      return json(201, created);
    }

    const approve = pathname.match(/^\/admin\/users\/(\d+)\/approve$/);
    if (approve && method === "POST") {
      const user = users.find(
        (candidate) => candidate.id === Number(approve[1]),
      )!;
      user.status = "ACTIVE";
      user.role = body.role;
      return json(200, user);
    }

    const role = pathname.match(/^\/admin\/users\/(\d+)\/role$/);
    if (role && method === "PATCH") {
      const user = users.find((candidate) => candidate.id === Number(role[1]))!;
      user.role = body.role;
      return json(200, user);
    }

    const remove = pathname.match(/^\/admin\/users\/(\d+)$/);
    if (remove && method === "DELETE") {
      const index = users.findIndex(
        (candidate) => candidate.id === Number(remove[1]),
      );
      if (users[index].status === "PENDING") {
        users.splice(index, 1);
      } else {
        users[index].status = "REMOVED";
      }
      return json(204, null);
    }

    throw new Error(`unhandled ${method} ${pathname}`);
  });
}

function useBackend() {
  global.fetch = fakeBackend() as unknown as typeof fetch;
  setAuthToken(null);
}

function rowFor(name: string): HTMLElement {
  return screen.getByText(name).closest("li") as HTMLElement;
}

/** Clicks the confirm button in the confirmation dialog that a critical action pops up. */
async function confirmInDialog(label: string) {
  const dialog = await screen.findByRole("alertdialog");
  fireEvent.click(within(dialog).getByRole("button", { name: label }));
  await waitFor(() =>
    expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument(),
  );
}

describe("AdminUsersPage", () => {
  it("approves a pending user with the chosen role and moves them out of the pending list", async () => {
    useBackend();
    renderWithQueryClient(<AdminUsersPage />);
    await screen.findByText("Percy Pending");

    const pendingRow = rowFor("Percy Pending");
    fireEvent.change(
      within(pendingRow).getByLabelText("Role for Percy Pending"),
      {
        target: { value: "MANAGER" },
      },
    );
    fireEvent.click(
      within(pendingRow).getByRole("button", { name: "Approve" }),
    );
    await confirmInDialog("Approve");

    await screen.findByText("No pending signups.");
    expect(
      within(rowFor("Percy Pending")).getByLabelText("Role for Percy Pending"),
    ).toHaveValue("MANAGER");
  });

  it("does nothing when the approve confirmation is dismissed", async () => {
    useBackend();
    renderWithQueryClient(<AdminUsersPage />);
    await screen.findByText("Percy Pending");

    fireEvent.click(
      within(rowFor("Percy Pending")).getByRole("button", { name: "Approve" }),
    );
    const dialog = await screen.findByRole("alertdialog");
    fireEvent.click(within(dialog).getByRole("button", { name: "Cancel" }));

    await waitFor(() =>
      expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument(),
    );
    expect(screen.getByText("Percy Pending")).toBeInTheDocument();
  });

  it("rejects a pending user and removes them entirely", async () => {
    useBackend();
    renderWithQueryClient(<AdminUsersPage />);
    await screen.findByText("Percy Pending");

    fireEvent.click(
      within(rowFor("Percy Pending")).getByRole("button", { name: "Reject" }),
    );
    await confirmInDialog("Reject");

    await waitFor(() =>
      expect(screen.queryByText("Percy Pending")).not.toBeInTheDocument(),
    );
  });

  it("changes an active user's role after confirmation", async () => {
    useBackend();
    renderWithQueryClient(<AdminUsersPage />);
    await screen.findByText("Ada Active");

    const select = within(rowFor("Ada Active")).getByLabelText(
      "Role for Ada Active",
    );
    expect(select).toHaveValue("MEMBER");
    fireEvent.change(select, { target: { value: "MANAGER" } });
    await confirmInDialog("Change role");

    await waitFor(() =>
      expect(
        within(rowFor("Ada Active")).getByLabelText("Role for Ada Active"),
      ).toHaveValue("MANAGER"),
    );
  });

  it("removes an active user after confirmation", async () => {
    useBackend();
    renderWithQueryClient(<AdminUsersPage />);
    await screen.findByText("Ada Active");

    fireEvent.click(
      within(rowFor("Ada Active")).getByRole("button", { name: "Remove" }),
    );
    await confirmInDialog("Remove user");

    await waitFor(() =>
      expect(screen.queryByText("Ada Active")).not.toBeInTheDocument(),
    );
  });

  it("directly creates an active user who then appears in the active list", async () => {
    useBackend();
    renderWithQueryClient(<AdminUsersPage />);
    await screen.findByText("Ada Active");

    fireEvent.change(screen.getByLabelText("Name"), {
      target: { value: "Nora New" },
    });
    fireEvent.change(screen.getByLabelText("Email"), {
      target: { value: "nora@example.com" },
    });
    fireEvent.change(screen.getByLabelText("Password"), {
      target: { value: "password123" },
    });
    fireEvent.change(screen.getByLabelText("Role"), {
      target: { value: "MANAGER" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Create user" }));
    await confirmInDialog("Create user");

    expect(await screen.findByText("Nora New")).toBeInTheDocument();
    expect(
      within(rowFor("Nora New")).getByLabelText("Role for Nora New"),
    ).toHaveValue("MANAGER");
  });
});
