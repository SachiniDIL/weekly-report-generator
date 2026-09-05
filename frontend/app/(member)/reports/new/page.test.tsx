import { fireEvent, screen, waitFor } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import { renderWithQueryClient } from "@/lib/test-render";
import NewReportPage from "./page";

const replaceMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn(), replace: replaceMock }),
}));

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
  weekStart: "2026-09-01",
  weekEnd: "2026-09-05",
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

describe("NewReportPage", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
    jest.clearAllMocks();
    fetchMock.mockImplementation((url: string, init: RequestInit) => {
      if (url.includes("/projects")) {
        return Promise.resolve(
          jsonResponse(200, [{ id: 5, name: "Apollo", description: null, active: true }]),
        );
      }
      if (url.endsWith("/reports") && init.method === "POST") {
        return Promise.resolve(jsonResponse(201, CREATED_REPORT));
      }
      throw new Error(`unexpected request: ${init.method} ${url}`);
    });
  });

  it("creates a report with content and redirects to its edit page", async () => {
    renderWithQueryClient(<NewReportPage />);
    await screen.findByText("New weekly report");

    fireEvent.change(screen.getByLabelText("Project"), { target: { value: "5" } });
    fireEvent.change(screen.getByLabelText("Week start"), { target: { value: "2026-09-01" } });
    fireEvent.change(screen.getByLabelText("Week end"), { target: { value: "2026-09-05" } });
    fireEvent.change(screen.getByLabelText("Notes"), { target: { value: "Kicked off" } });

    fireEvent.click(screen.getByRole("button", { name: "Add task" }));
    fireEvent.change(screen.getByLabelText("Task name"), { target: { value: "Design schema" } });
    fireEvent.change(screen.getByLabelText("Priority"), { target: { value: "HIGH" } });
    fireEvent.change(screen.getByLabelText("Status"), { target: { value: "IN_PROGRESS" } });

    fireEvent.click(screen.getByRole("button", { name: "Save draft" }));

    await waitFor(() => expect(replaceMock).toHaveBeenCalledWith("/reports/99"));

    const postCall = fetchMock.mock.calls.find(
      ([url, init]) => url.endsWith("/reports") && init.method === "POST",
    );
    const body = JSON.parse(postCall![1].body as string);
    expect(body).toMatchObject({
      projectId: 5,
      weekStart: "2026-09-01",
      weekEnd: "2026-09-05",
      content: {
        notes: "Kicked off",
        taskEntries: [{ taskName: "Design schema", priority: "HIGH", status: "IN_PROGRESS" }],
      },
    });
  });

  it("blocks submission and shows problems when required fields are missing", async () => {
    renderWithQueryClient(<NewReportPage />);
    await screen.findByText("New weekly report");

    fireEvent.click(screen.getByRole("button", { name: "Save draft" }));

    expect(await screen.findByText("Choose a project.")).toBeInTheDocument();
    expect(
      fetchMock.mock.calls.some(([url, init]) => url.endsWith("/reports") && init.method === "POST"),
    ).toBe(false);
  });
});
