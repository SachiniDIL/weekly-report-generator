import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { ReportResponse } from "@/lib/api/reports";
import { renderWithQueryClient } from "@/lib/test-render";
import EditReportPage from "./page";

const replaceMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn(), replace: replaceMock }),
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
    replaceMock.mockReset();
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
    // Save draft is offered at the top and bottom of the form.
    expect(screen.getAllByRole("button", { name: "Save draft" })).toHaveLength(
      2,
    );
  });

  it("asks for confirmation before submitting for review", async () => {
    const draft = report({
      status: "DRAFT",
      content: { ...EMPTY_CONTENT, notes: "Ready" },
    });
    fetchMock.mockImplementation((url: string, init?: RequestInit) => {
      if (url.endsWith("/reports/7/versions"))
        return Promise.resolve(jsonResponse(200, []));
      if (url.endsWith("/reports/7/submit") && init?.method === "POST") {
        return Promise.resolve(
          jsonResponse(200, report({ status: "SUBMITTED" })),
        );
      }
      if (url.endsWith("/reports/7") && init?.method === "PUT") {
        return Promise.resolve(jsonResponse(200, draft));
      }
      if (url.endsWith("/reports/7"))
        return Promise.resolve(jsonResponse(200, draft));
      throw new Error(`unexpected request: ${url}`);
    });

    renderWithQueryClient(<EditReportPage />);
    await screen.findByDisplayValue("Ready");

    fireEvent.click(screen.getByRole("button", { name: "Submit for review" }));

    const dialog = await screen.findByRole("alertdialog");
    fireEvent.click(
      within(dialog).getByRole("button", { name: "Submit for review" }),
    );

    await waitFor(() => expect(replaceMock).toHaveBeenCalledWith("/reports/7"));
    expect(
      fetchMock.mock.calls.some(
        ([url, init]) =>
          url.endsWith("/reports/7/submit") && init?.method === "POST",
      ),
    ).toBe(true);
  });

  it("shows the correction comment and the earlier versions for a NEEDS_CORRECTION report", async () => {
    respondWith(report({ status: "NEEDS_CORRECTION", currentVersionNo: 2 }), [
      {
        content: { ...EMPTY_CONTENT, versionNo: 2, reportVersionId: 2 },
        reviewComment: null,
      },
      {
        content: { ...EMPTY_CONTENT, versionNo: 1, reportVersionId: 1 },
        reviewComment: {
          action: "CHANGES_REQUESTED",
          comment: "Add the hours breakdown before resubmitting",
          managerName: "Dana",
          createdAt: "2026-09-06T00:00:00Z",
        },
      },
    ]);

    renderWithQueryClient(<EditReportPage />);

    const notice = await screen.findByRole("alert");
    expect(
      within(notice).getByText("Add the hours breakdown before resubmitting"),
    ).toBeInTheDocument();
    expect(within(notice).getByText("Changes requested")).toBeInTheDocument();

    const priorVersions = await screen.findByRole("region", {
      name: "Previous versions",
    });
    expect(within(priorVersions).getByText("Version 1")).toBeInTheDocument();
    expect(
      within(priorVersions).getByText(/Changes requested by Dana/),
    ).toBeInTheDocument();
  });

  it("renders a SUBMITTED report read-only, with no editing controls", async () => {
    respondWith(
      report({
        status: "SUBMITTED",
        content: { ...EMPTY_CONTENT, tasksPlannedNext: "Ship the API" },
      }),
    );

    renderWithQueryClient(<EditReportPage />);

    expect(await screen.findByText("Ship the API")).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Save draft" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Submit for review" }),
    ).not.toBeInTheDocument();
  });

  it("lets a member see the previous versions of a read-only report", async () => {
    respondWith(report({ status: "APPROVED", currentVersionNo: 2 }), [
      {
        content: { ...EMPTY_CONTENT, versionNo: 2, reportVersionId: 2 },
        reviewComment: {
          action: "APPROVED",
          comment: null,
          managerName: "Dana",
          createdAt: "2026-09-08T00:00:00Z",
        },
      },
      {
        content: { ...EMPTY_CONTENT, versionNo: 1, reportVersionId: 1 },
        reviewComment: {
          action: "CHANGES_REQUESTED",
          comment: "Fix the numbers",
          managerName: "Dana",
          createdAt: "2026-09-06T00:00:00Z",
        },
      },
    ]);

    renderWithQueryClient(<EditReportPage />);

    const priorVersions = await screen.findByRole("region", {
      name: "Previous versions",
    });
    expect(within(priorVersions).getByText("Version 1")).toBeInTheDocument();
    expect(
      within(priorVersions).getByText("Fix the numbers"),
    ).toBeInTheDocument();
  });

  it("shows no previous-versions section for a report that only ever had one version", async () => {
    respondWith(report({ status: "APPROVED", currentVersionNo: 1 }), []);

    renderWithQueryClient(<EditReportPage />);

    await screen.findByText("Apollo");
    expect(
      screen.queryByRole("region", { name: "Previous versions" }),
    ).not.toBeInTheDocument();
  });
});
