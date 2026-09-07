import { fireEvent, screen, waitFor } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { ReportResponse } from "@/lib/api/reports";
import { renderWithQueryClient } from "@/lib/test-render";
import ReviewReportPage from "./page";

const pushMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: pushMock,
    replace: jest.fn(),
    prefetch: jest.fn(),
    back: jest.fn(),
  }),
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
    status: "SUBMITTED",
    currentVersionNo: 1,
    content: EMPTY_CONTENT,
    ...overrides,
  };
}

describe("ReviewReportPage", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  function respondWith(detail: ReportResponse, history: unknown[] = []) {
    fetchMock.mockImplementation((url: string, init: RequestInit) => {
      if (url.includes("/reports/7/review") && init.method === "POST") {
        return Promise.resolve(
          jsonResponse(200, { ...detail, status: "APPROVED" }),
        );
      }
      if (url.endsWith("/reports/7/versions")) {
        return Promise.resolve(jsonResponse(200, history));
      }
      if (url.endsWith("/reports/7")) {
        return Promise.resolve(jsonResponse(200, detail));
      }
      throw new Error(`unexpected request: ${init.method} ${url}`);
    });
  }

  function reviewCallBody() {
    const call = fetchMock.mock.calls.find(
      ([url, init]) =>
        url.includes("/reports/7/review") && init.method === "POST",
    );
    return call ? JSON.parse(call[1].body as string) : undefined;
  }

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
    pushMock.mockReset();
  });

  it("approves a submitted report and redirects to /projects", async () => {
    respondWith(report({ status: "SUBMITTED" }));

    renderWithQueryClient(<ReviewReportPage />);

    fireEvent.click(await screen.findByRole("button", { name: "Approve" }));

    await waitFor(() =>
      expect(reviewCallBody()).toEqual({ action: "APPROVED", comment: null }),
    );
    await waitFor(() => expect(pushMock).toHaveBeenCalledWith("/projects"));
  });

  it("blocks a change request with no comment before calling the API", async () => {
    respondWith(report({ status: "SUBMITTED" }));

    renderWithQueryClient(<ReviewReportPage />);

    fireEvent.click(
      await screen.findByRole("radio", { name: "Request changes" }),
    );
    fireEvent.click(screen.getByRole("button", { name: "Request changes" }));

    expect(
      await screen.findByText(/Explain what needs to change/i),
    ).toBeInTheDocument();
    expect(reviewCallBody()).toBeUndefined();
  });

  it("submits a change request with a comment", async () => {
    respondWith(report({ status: "SUBMITTED" }));

    renderWithQueryClient(<ReviewReportPage />);

    fireEvent.click(
      await screen.findByRole("radio", { name: "Request changes" }),
    );
    fireEvent.change(screen.getByLabelText("What needs to change"), {
      target: { value: "Add the hours breakdown" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Request changes" }));

    await waitFor(() =>
      expect(reviewCallBody()).toEqual({
        action: "CHANGES_REQUESTED",
        comment: "Add the hours breakdown",
      }),
    );
    await waitFor(() => expect(pushMock).toHaveBeenCalledWith("/projects"));
  });

  it("shows a non-SUBMITTED report read-only with no review actions", async () => {
    respondWith(
      report({
        status: "APPROVED",
        content: { ...EMPTY_CONTENT, tasksPlannedNext: "Ship the API" },
      }),
    );

    renderWithQueryClient(<ReviewReportPage />);

    expect(await screen.findByText("Ship the API")).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Approve" }),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Request changes" }),
    ).not.toBeInTheDocument();
    expect(
      screen.getByText(/review actions are only available/i),
    ).toBeInTheDocument();
  });
});
