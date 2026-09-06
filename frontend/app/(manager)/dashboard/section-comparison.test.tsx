import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import { renderWithQueryClient } from "@/lib/test-render";
import { SectionComparison } from "./section-comparison";

function json(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as unknown as Response;
}

const BLOCKERS = [
  {
    memberName: "Alice",
    status: "SUBMITTED",
    items: [
      { description: "Blocked on staging credentials", key: true },
      { description: "Waiting on design review", key: false },
    ],
  },
  { memberName: "Bob", status: "DRAFT", items: [] },
  { memberName: "Carol", status: "NOT_STARTED", items: [] },
];

const ACHIEVEMENTS = [
  {
    memberName: "Alice",
    status: "SUBMITTED",
    items: [{ description: "Shipped the billing export", key: true }],
  },
  { memberName: "Bob", status: "DRAFT", items: [] },
  { memberName: "Carol", status: "NOT_STARTED", items: [] },
];

function sectionBackend() {
  return jest.fn(async (rawUrl: string) => {
    const url = new URL(rawUrl);
    if (url.pathname !== "/dashboard/section") {
      throw new Error(`unexpected request: ${url.pathname}`);
    }
    const section = url.searchParams.get("section");
    return json(section === "achievements" ? ACHIEVEMENTS : BLOCKERS);
  });
}

describe("SectionComparison", () => {
  let fetchMock: jest.Mock;

  beforeEach(() => {
    fetchMock = sectionBackend();
    global.fetch = fetchMock as unknown as typeof fetch;
    setAuthToken(null);
  });

  it("requests blockers by default and switches to achievements with the right section param", async () => {
    renderWithQueryClient(<SectionComparison />);

    expect(await screen.findByText("Blocked on staging credentials")).toBeInTheDocument();
    expect(new URL(fetchMock.mock.calls[0][0]).searchParams.get("section")).toBe("blockers");

    fireEvent.click(screen.getByRole("button", { name: "Achievements" }));

    expect(await screen.findByText("Shipped the billing export")).toBeInTheDocument();
    expect(screen.queryByText("Blocked on staging credentials")).not.toBeInTheDocument();

    const achievementsCall = fetchMock.mock.calls.find(
      ([requestedUrl]) => new URL(requestedUrl).searchParams.get("section") === "achievements",
    );
    expect(achievementsCall).toBeDefined();
  });

  it("sends the current ISO week as the default week range", async () => {
    renderWithQueryClient(<SectionComparison />);

    await screen.findByText("Blocked on staging credentials");

    const url = new URL(fetchMock.mock.calls[0][0]);
    const weekStart = new Date(`${url.searchParams.get("weekStart")}T00:00:00`);
    const weekEnd = new Date(`${url.searchParams.get("weekEnd")}T00:00:00`);
    expect(weekStart.getDay()).toBe(1);
    expect(weekEnd.getDay()).toBe(0);
  });

  it("shows the status and no items for a member with no report or a draft", async () => {
    renderWithQueryClient(<SectionComparison />);

    const bobRow = (await screen.findByText("Bob")).closest("li")!;
    expect(within(bobRow).getByText("Draft")).toBeInTheDocument();
    expect(within(bobRow).getByText(/no blockers recorded for this week/i)).toBeInTheDocument();
    expect(within(bobRow).queryByText(/waiting on/i)).not.toBeInTheDocument();

    const carolRow = screen.getByText("Carol").closest("li")!;
    expect(within(carolRow).getByText("Not started")).toBeInTheDocument();
    expect(within(carolRow).getByText(/no blockers recorded for this week/i)).toBeInTheDocument();
  });

  it("visually distinguishes the key item from non-key items", async () => {
    renderWithQueryClient(<SectionComparison />);

    const keyItem = (await screen.findByText("Blocked on staging credentials")).closest("li")!;
    const plainItem = screen.getByText("Waiting on design review").closest("li")!;

    expect(within(keyItem).getByText("Key issue")).toBeInTheDocument();
    expect(keyItem.className).toMatch(/amber/);

    expect(within(plainItem).queryByText("Key issue")).not.toBeInTheDocument();
    expect(plainItem.className).not.toMatch(/amber/);
  });

  it("labels the key achievement differently from the key blocker", async () => {
    renderWithQueryClient(<SectionComparison />);

    await screen.findByText("Blocked on staging credentials");
    fireEvent.click(screen.getByRole("button", { name: "Achievements" }));

    const keyItem = (await screen.findByText("Shipped the billing export")).closest("li")!;
    expect(within(keyItem).getByText("Key highlight")).toBeInTheDocument();
  });
});
