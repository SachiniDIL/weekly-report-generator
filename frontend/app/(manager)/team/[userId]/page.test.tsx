import { screen, waitFor, within } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { MemberProfile } from "@/lib/api/dashboard";
import type { Page, ReportListItemView } from "@/lib/api/reports";
import { renderWithQueryClient } from "@/lib/test-render";
import TeamMemberProfilePage from "./page";

jest.mock("next/navigation", () => ({
  useParams: () => ({ userId: "5" }),
}));

function jsonResponse(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

const PROFILE: MemberProfile = {
  id: 5,
  name: "Fatima Noor",
  email: "fatima@example.com",
  totalReportsSubmitted: 9,
  needsCorrectionCount: 4,
  hoursByTaskType: [
    { taskType: "DEV", totalHours: 12 },
    { taskType: "REVIEW", totalHours: 3.5 },
  ],
};

function reportRow(overrides: Partial<ReportListItemView>): ReportListItemView {
  return {
    id: 41,
    status: "APPROVED",
    weekStart: "2026-08-24",
    weekEnd: "2026-08-30",
    ownerName: "Fatima Noor",
    projectName: "Apollo",
    currentVersionNo: 1,
    ...overrides,
  };
}

function pageOf(content: ReportListItemView[]): Page<ReportListItemView> {
  return {
    content,
    totalElements: content.length,
    totalPages: 1,
    number: 0,
    size: 25,
    first: true,
    last: true,
    numberOfElements: content.length,
    empty: content.length === 0,
  };
}

const REPORTS = [
  reportRow({ id: 41, weekStart: "2026-08-24", weekEnd: "2026-08-30", status: "APPROVED" }),
  reportRow({
    id: 37,
    weekStart: "2026-08-17",
    weekEnd: "2026-08-23",
    status: "NEEDS_CORRECTION",
    projectName: "Zephyr",
  }),
];

describe("TeamMemberProfilePage", () => {
  const fetchMock = jest.fn<Promise<Response>, [string]>();

  function respondWith({
    profile = jsonResponse(200, PROFILE),
    reports = jsonResponse(200, pageOf(REPORTS)),
  }: {
    profile?: Response;
    reports?: Response;
  }) {
    fetchMock.mockImplementation((url: string) => {
      if (url.includes("/dashboard/team/5")) return Promise.resolve(profile);
      if (url.includes("/reports?")) return Promise.resolve(reports);
      throw new Error(`unexpected request: ${url}`);
    });
  }

  function reportsRequestUrl(): URL | undefined {
    const call = fetchMock.mock.calls.find(([url]) => url.includes("/reports?"));
    return call ? new URL(call[0]) : undefined;
  }

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
  });

  it("renders the member's identity and stats from the profile response", async () => {
    respondWith({});

    renderWithQueryClient(<TeamMemberProfilePage />);

    expect(await screen.findByRole("heading", { name: "Fatima Noor" })).toBeInTheDocument();
    expect(screen.getByText("fatima@example.com")).toBeInTheDocument();

    const submitted = screen.getByText("Reports submitted").closest("div")!;
    expect(within(submitted).getByText("9")).toBeInTheDocument();

    const corrections = screen.getByText("Reports needing correction").closest("div")!;
    expect(within(corrections).getByText("4")).toBeInTheDocument();

    const hours = screen.getByText("Hours by task type").closest("div")!;
    expect(within(hours).getByText("DEV")).toBeInTheDocument();
    expect(within(hours).getByText("12h")).toBeInTheDocument();
    expect(within(hours).getByText("REVIEW")).toBeInTheDocument();
    expect(within(hours).getByText("3.5h")).toBeInTheDocument();
  });

  it("lists this member's reports, scoped to their userId, linking each row to its review page", async () => {
    respondWith({});

    renderWithQueryClient(<TeamMemberProfilePage />);

    expect(await screen.findByText(/2026-08-24.*2026-08-30/)).toBeInTheDocument();
    expect(screen.getByText(/2026-08-17.*2026-08-23/)).toBeInTheDocument();

    await waitFor(() => expect(reportsRequestUrl()).toBeDefined());
    expect(reportsRequestUrl()!.searchParams.get("userId")).toBe("5");

    expect(screen.getByRole("link", { name: /2026-08-24/ })).toHaveAttribute(
      "href",
      "/reports/41/review",
    );
  });

  it("shows an empty-state message when the member has no reports", async () => {
    respondWith({ reports: jsonResponse(200, pageOf([])) });

    renderWithQueryClient(<TeamMemberProfilePage />);

    expect(await screen.findByText(/hasn't written any reports yet/i)).toBeInTheDocument();
  });

  it("shows a clear message for an unknown or non-member user id", async () => {
    respondWith({ profile: jsonResponse(404, { message: "User 5 not found" }) });

    renderWithQueryClient(<TeamMemberProfilePage />);

    expect(await screen.findByText(/team member was not found/i)).toBeInTheDocument();
    expect(screen.queryByText("User 5 not found")).not.toBeInTheDocument();
    expect(screen.queryByText("Report history")).not.toBeInTheDocument();
  });
});
