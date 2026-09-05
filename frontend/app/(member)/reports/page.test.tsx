import { fireEvent, screen, waitFor } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { Page, ReportListItemView } from "@/lib/api/reports";
import { renderWithQueryClient } from "@/lib/test-render";
import ReportHistoryPage from "./page";

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn(), replace: jest.fn(), prefetch: jest.fn() }),
}));

function jsonResponse(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

function item(overrides: Partial<ReportListItemView>): ReportListItemView {
  return {
    id: 1,
    status: "DRAFT",
    weekStart: "2026-09-01",
    weekEnd: "2026-09-05",
    ownerName: "Lin",
    projectName: "Apollo",
    currentVersionNo: 1,
    ...overrides,
  };
}

function pageOf(
  content: ReportListItemView[],
  overrides: Partial<Page<ReportListItemView>> = {},
): Page<ReportListItemView> {
  return {
    content,
    totalElements: content.length,
    totalPages: content.length === 0 ? 0 : 1,
    number: 0,
    size: 25,
    first: true,
    last: true,
    numberOfElements: content.length,
    empty: content.length === 0,
    ...overrides,
  };
}

describe("ReportHistoryPage", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
  });

  it("renders each report with its status indicator and a link to the report", async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(
        200,
        pageOf([
          item({ id: 1, status: "DRAFT", projectName: "Apollo" }),
          item({ id: 2, status: "NEEDS_CORRECTION", projectName: "Zephyr" }),
          item({ id: 3, status: "APPROVED", projectName: "Helios" }),
        ]),
      ),
    );

    renderWithQueryClient(<ReportHistoryPage />);

    expect(await screen.findByText("Draft")).toBeInTheDocument();
    expect(screen.getByText("Needs correction")).toBeInTheDocument();
    expect(screen.getByText("Approved")).toBeInTheDocument();

    expect(screen.getByText("Zephyr").closest("a")).toHaveAttribute("href", "/reports/2");
  });

  it("shows a Create new report link", async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, pageOf([item({})])));

    renderWithQueryClient(<ReportHistoryPage />);
    await screen.findByText("Draft");

    expect(screen.getByRole("link", { name: "Create new report" })).toHaveAttribute(
      "href",
      "/reports/new",
    );
  });

  it("renders a friendly empty state with zero reports", async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, pageOf([])));

    renderWithQueryClient(<ReportHistoryPage />);

    expect(await screen.findByText(/haven't written any reports yet/i)).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Create new report" })).toBeInTheDocument();
    expect(screen.queryByRole("navigation", { name: "Pagination" })).not.toBeInTheDocument();
  });

  it("pages forward through more than one page of results", async () => {
    fetchMock.mockImplementation((url: string) => {
      const onSecondPage = url.includes("page=1");
      return Promise.resolve(
        jsonResponse(
          200,
          pageOf([item({ id: onSecondPage ? 2 : 1, projectName: onSecondPage ? "Week B" : "Week A" })], {
            totalPages: 2,
            totalElements: 2,
            number: onSecondPage ? 1 : 0,
            first: !onSecondPage,
            last: onSecondPage,
          }),
        ),
      );
    });

    renderWithQueryClient(<ReportHistoryPage />);

    expect(await screen.findByText("Week A")).toBeInTheDocument();
    expect(screen.getByText("Page 1 of 2")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Next" }));

    expect(await screen.findByText("Week B")).toBeInTheDocument();
    expect(screen.getByText("Page 2 of 2")).toBeInTheDocument();
    await waitFor(() =>
      expect(fetchMock.mock.calls.some(([requestUrl]) => requestUrl.includes("page=1"))).toBe(true),
    );
  });
});
