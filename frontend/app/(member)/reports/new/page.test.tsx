import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import type { ReportListItemView } from "@/lib/api/reports";
import { recentWeeks } from "@/lib/reports/recent-weeks";
import { renderWithQueryClient } from "@/lib/test-render";
import NewReportPage from "./page";

const replaceMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn(), replace: replaceMock }),
}));

const [THIS_WEEK] = recentWeeks(1);

function jsonResponse(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

const CREATED_REPORT = {
  id: 99,
  projectId: 5,
  projectName: "Apollo",
  userId: 1,
  ownerName: "Lin",
  weekStart: THIS_WEEK.weekStart,
  weekEnd: THIS_WEEK.weekEnd,
  status: "DRAFT",
  currentVersionNo: 1,
  content: {
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
  },
};

function existingReport(
  overrides: Partial<ReportListItemView>,
): ReportListItemView {
  return {
    id: 7,
    status: "SUBMITTED",
    weekStart: THIS_WEEK.weekStart,
    weekEnd: THIS_WEEK.weekEnd,
    ownerName: "Lin",
    projectId: 5,
    projectName: "Apollo",
    currentVersionNo: 1,
    ...overrides,
  };
}

describe("NewReportPage", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  /** Serves the assigned-projects list and the member's own reports (coverage). */
  function backend({
    projects = [
      { id: 5, name: "Apollo", description: null, active: true },
      { id: 6, name: "Zephyr", description: null, active: true },
    ],
    myReports = [] as ReportListItemView[],
    createStatus = 201,
    createBody = CREATED_REPORT as unknown,
  } = {}) {
    fetchMock.mockImplementation((url: string, init: RequestInit) => {
      if (url.includes("/me/projects")) {
        return Promise.resolve(jsonResponse(200, projects));
      }
      if (url.includes("/reports?") && (init.method ?? "GET") === "GET") {
        return Promise.resolve(
          jsonResponse(200, {
            content: myReports,
            totalElements: myReports.length,
            totalPages: 1,
            number: 0,
            size: 100,
            first: true,
            last: true,
            numberOfElements: myReports.length,
            empty: myReports.length === 0,
          }),
        );
      }
      if (url.endsWith("/reports") && init.method === "POST") {
        return Promise.resolve(jsonResponse(createStatus, createBody));
      }
      throw new Error(`unexpected request: ${init.method} ${url}`);
    });
  }

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    replaceMock.mockReset();
    setAuthToken(null);
  });

  it("creates a report for the chosen project and week, then redirects", async () => {
    backend();
    renderWithQueryClient(<NewReportPage />);
    await screen.findByText("New weekly report");

    fireEvent.change(screen.getByLabelText("Project"), {
      target: { value: "5" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Add task" }));
    fireEvent.change(screen.getByLabelText("Task name"), {
      target: { value: "Design schema" },
    });
    fireEvent.change(screen.getByLabelText("Priority"), {
      target: { value: "HIGH" },
    });
    fireEvent.change(screen.getByLabelText("Status"), {
      target: { value: "IN_PROGRESS" },
    });
    fireEvent.click(screen.getAllByRole("button", { name: "Save draft" })[0]);

    await waitFor(() =>
      expect(replaceMock).toHaveBeenCalledWith("/reports/99"),
    );

    const postCall = fetchMock.mock.calls.find(
      ([url, init]) => url.endsWith("/reports") && init.method === "POST",
    );
    const body = JSON.parse(postCall![1].body as string);
    expect(body).toMatchObject({
      projectId: 5,
      weekStart: THIS_WEEK.weekStart,
      weekEnd: THIS_WEEK.weekEnd,
      content: {
        taskEntries: [{ taskName: "Design schema", priority: "HIGH" }],
      },
    });
  });

  it("only offers past and current weeks, never an upcoming one", async () => {
    backend();
    renderWithQueryClient(<NewReportPage />);
    await screen.findByText("New weekly report");

    const options = within(screen.getByLabelText("Week")).getAllByRole(
      "option",
    ) as HTMLOptionElement[];
    expect(options[0].textContent).toContain("This week");
    for (const option of options) {
      const start = option.textContent!.match(/\d{4}-\d{2}-\d{2}/)![0];
      expect(start <= THIS_WEEK.weekStart).toBe(true);
    }
  });

  it("hides the editor until a project is chosen", async () => {
    backend();
    renderWithQueryClient(<NewReportPage />);
    await screen.findByText("New weekly report");

    expect(
      screen.getByText(/Choose a project and week to start/i),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Save draft" }),
    ).not.toBeInTheDocument();
  });

  it("shows what the member already has, and blocks a project already reported this week", async () => {
    backend({ myReports: [existingReport({ id: 7, status: "SUBMITTED" })] });
    renderWithQueryClient(<NewReportPage />);
    await screen.findByText("Where you stand");

    // The coverage table shows the existing report's status.
    const apolloRow = screen.getByText("Apollo").closest("tr")!;
    expect(within(apolloRow).getByText("Submitted")).toBeInTheDocument();

    // The project can't be picked for this week.
    const apolloOption = within(screen.getByLabelText("Project")).getByRole(
      "option",
      { name: /Apollo/ },
    ) as HTMLOptionElement;
    expect(apolloOption.disabled).toBe(true);
    expect(apolloOption.textContent).toMatch(/already started/i);
  });

  it("surfaces the backend message if the report turns out to already exist", async () => {
    backend({
      createStatus: 409,
      createBody: {
        message: "You already have a report for this project and week.",
      },
    });
    renderWithQueryClient(<NewReportPage />);
    await screen.findByText("New weekly report");

    fireEvent.change(screen.getByLabelText("Project"), {
      target: { value: "6" },
    });
    fireEvent.click(screen.getAllByRole("button", { name: "Save draft" })[0]);

    expect(
      await screen.findByText(
        "You already have a report for this project and week.",
      ),
    ).toBeInTheDocument();
    expect(replaceMock).not.toHaveBeenCalled();
  });
});
