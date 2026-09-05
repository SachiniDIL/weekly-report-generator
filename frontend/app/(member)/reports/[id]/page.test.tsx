import { screen } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { ReportResponse } from "@/lib/api/reports";
import { renderWithQueryClient } from "@/lib/test-render";
import EditReportPage from "./page";

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn(), replace: jest.fn() }),
  useParams: () => ({ id: "7" }),
}));

function jsonResponse(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

const EMPTY_CONTENT = {
  reportVersionId: 1,
  versionNo: 1,
  submittedAt: "2026-09-05T00:00:00Z",
  tasksPlannedNext: null,
  notes: null,
  links: null,
  taskEntries: [],
  blockers: [],
  achievements: [],
  hoursBreakdown: [],
};

function report(overrides: Partial<ReportResponse>): ReportResponse {
  return {
    id: 7,
    projectId: 5,
    projectName: "Apollo",
    userId: 1,
    ownerName: "Lin",
    weekStart: "2026-09-01",
    weekEnd: "2026-09-05",
    status: "DRAFT",
    currentVersionNo: 1,
    content: EMPTY_CONTENT,
    ...overrides,
  };
}

describe("EditReportPage", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  function respondWith(detail: ReportResponse, history: unknown[] = []) {
    fetchMock.mockImplementation((url: string) => {
      if (url.endsWith("/reports/7/versions")) {
        return Promise.resolve(jsonResponse(200, history));
      }
      if (url.endsWith("/reports/7")) {
        return Promise.resolve(jsonResponse(200, detail));
      }
      throw new Error(`unexpected request: ${url}`);
    });
  }

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
  });

  it("loads an existing draft's content into editable fields", async () => {
    respondWith(
      report({
        status: "DRAFT",
        content: {
          ...EMPTY_CONTENT,
          notes: "Halfway there",
          taskEntries: [
            {
              id: 11,
              taskName: "Wire up auth",
              priority: "HIGH",
              plannedPct: 100,
              actualPct: 40,
              status: "WIP",
              timePlanned: null,
              timeSpent: null,
              deliverable: null,
            },
          ],
        },
      }),
    );

    renderWithQueryClient(<EditReportPage />);

    expect(await screen.findByDisplayValue("Wire up auth")).toBeInTheDocument();
    expect(screen.getByDisplayValue("Halfway there")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Save draft" })).toBeInTheDocument();
  });

  it("shows the manager's correction comment for a NEEDS_CORRECTION report", async () => {
    respondWith(report({ status: "NEEDS_CORRECTION" }), [
      { content: { ...EMPTY_CONTENT, versionNo: 2 }, reviewComment: null },
      {
        content: { ...EMPTY_CONTENT, versionNo: 1 },
        reviewComment: {
          action: "CHANGES_REQUESTED",
          comment: "Add the hours breakdown before resubmitting",
          managerName: "Dana",
          createdAt: "2026-09-06T00:00:00Z",
        },
      },
    ]);

    renderWithQueryClient(<EditReportPage />);

    expect(
      await screen.findByText("Add the hours breakdown before resubmitting"),
    ).toBeInTheDocument();
    expect(screen.getByText("Changes requested")).toBeInTheDocument();
  });

  it("renders a SUBMITTED report read-only, with no editing controls", async () => {
    respondWith(
      report({ status: "SUBMITTED", content: { ...EMPTY_CONTENT, tasksPlannedNext: "Ship the API" } }),
    );

    renderWithQueryClient(<EditReportPage />);

    expect(await screen.findByText("Ship the API")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Save draft" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Submit for review" })).not.toBeInTheDocument();
  });
});
