import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import type { Role } from "@/lib/api-client";
import { renderWithQueryClient } from "@/lib/test-render";
import { AppShell } from "./app-shell";

jest.mock("next/navigation", () => ({ usePathname: () => "/reports" }));

let mockAuthUser: { id: number; name: string; role: Role };
jest.mock("../auth-context", () => ({
  useAuth: () => ({ user: mockAuthUser, logout: jest.fn() }),
}));

function pageEnvelope(totalElements: number) {
  return {
    content: [],
    totalElements,
    totalPages: 1,
    number: 0,
    size: 1,
    first: true,
    last: true,
    numberOfElements: 0,
    empty: totalElements === 0,
  };
}

/** `countByStatus` maps a `?status=` value to the totalElements the fake API reports. */
function useApi(countByStatus: Record<string, number>) {
  global.fetch = jest.fn(async (rawUrl: string) => {
    const status = new URL(rawUrl).searchParams.get("status") ?? "";
    return {
      ok: true,
      status: 200,
      json: async () => pageEnvelope(countByStatus[status] ?? 0),
    } as unknown as Response;
  }) as unknown as typeof fetch;
}

function linkFor(name: string): HTMLElement {
  return screen.getByRole("link", { name: new RegExp(name) });
}

describe("AppShell sidebar badges", () => {
  it("shows a dot on the member's Reports tab when a report needs corrections", async () => {
    mockAuthUser = { id: 1, name: "Tess", role: "MEMBER" };
    useApi({ NEEDS_CORRECTION: 2 });

    renderWithQueryClient(<AppShell variant="member">content</AppShell>);

    await waitFor(() =>
      expect(linkFor("Reports")).toHaveTextContent("changes requested"),
    );
  });

  it("shows no dot when the member has no reports needing corrections", async () => {
    mockAuthUser = { id: 1, name: "Tess", role: "MEMBER" };
    useApi({ NEEDS_CORRECTION: 0 });

    renderWithQueryClient(<AppShell variant="member">content</AppShell>);

    await screen.findByRole("link", { name: /Reports/ });
    expect(screen.queryByText(/changes requested/)).not.toBeInTheDocument();
  });

  it("shows the count of reports awaiting review on the manager's Review Queue tab", async () => {
    mockAuthUser = { id: 9, name: "Mara", role: "MANAGER" };
    useApi({ SUBMITTED: 3 });

    renderWithQueryClient(<AppShell variant="manager">content</AppShell>);

    const reviewLink = await screen.findByRole("link", {
      name: /Review Queue: 3 waiting/,
    });
    expect(reviewLink).toHaveTextContent("3");
  });

  it("shows no count when nothing is awaiting review", async () => {
    mockAuthUser = { id: 9, name: "Mara", role: "MANAGER" };
    useApi({ SUBMITTED: 0 });

    renderWithQueryClient(<AppShell variant="manager">content</AppShell>);

    await screen.findByRole("link", { name: /Review Queue/ });
    expect(
      screen.queryByRole("link", { name: /waiting/ }),
    ).not.toBeInTheDocument();
  });
});

describe("AppShell mobile navigation", () => {
  beforeEach(() => {
    mockAuthUser = { id: 1, name: "Tess", role: "MEMBER" };
    useApi({});
  });

  it("opens a nav drawer from the menu button with the full nav inside", async () => {
    renderWithQueryClient(<AppShell variant="member">content</AppShell>);
    await screen.findByRole("button", { name: "Open menu" });

    expect(
      screen.queryByRole("dialog", { name: "Menu" }),
    ).not.toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Open menu" }));

    const drawer = screen.getByRole("dialog", { name: "Menu" });
    expect(
      within(drawer).getByRole("link", { name: /Reports/ }),
    ).toBeInTheDocument();
    expect(
      within(drawer).getByRole("link", { name: /New Report/ }),
    ).toBeInTheDocument();
  });

  it("closes the drawer from the close button and with Escape", async () => {
    renderWithQueryClient(<AppShell variant="member">content</AppShell>);

    fireEvent.click(await screen.findByRole("button", { name: "Open menu" }));
    fireEvent.click(screen.getByRole("button", { name: "Close menu" }));
    await waitFor(() =>
      expect(
        screen.queryByRole("dialog", { name: "Menu" }),
      ).not.toBeInTheDocument(),
    );

    fireEvent.click(screen.getByRole("button", { name: "Open menu" }));
    fireEvent.keyDown(document, { key: "Escape" });
    await waitFor(() =>
      expect(
        screen.queryByRole("dialog", { name: "Menu" }),
      ).not.toBeInTheDocument(),
    );
  });
});
