import { fireEvent, screen, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";
import { setAuthToken } from "@/lib/api-client";
import { AuthProvider } from "@/lib/auth-context";
import { renderWithQueryClient } from "@/lib/test-render";
import ManagerLayout from "../layout";
import ManagerDashboardPage from "./page";

const replaceMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: replaceMock,
    prefetch: jest.fn(),
  }),
  usePathname: () => "/dashboard",
}));

jest.mock("recharts", () => {
  function ResponsiveContainer({ children }: { children?: ReactNode }) {
    return <>{children}</>;
  }
  function BarChart({
    children,
    data,
  }: {
    children?: ReactNode;
    data?: unknown[];
  }) {
    return (
      <div data-testid="bar-chart" data-count={data?.length ?? 0}>
        {children}
      </div>
    );
  }
  function PieChart({ children }: { children?: ReactNode }) {
    return <div data-testid="pie-chart">{children}</div>;
  }
  function Pie({ children, data }: { children?: ReactNode; data?: unknown[] }) {
    return (
      <div data-testid="pie" data-count={data?.length ?? 0}>
        {children}
      </div>
    );
  }
  function Nothing() {
    return null;
  }
  return {
    ResponsiveContainer,
    BarChart,
    PieChart,
    Pie,
    Bar: Nothing,
    Cell: Nothing,
    XAxis: Nothing,
    YAxis: Nothing,
    CartesianGrid: Nothing,
    Tooltip: Nothing,
    Legend: Nothing,
  };
});

function json(body: unknown): Response {
  return {
    ok: true,
    status: 200,
    json: async () => body,
  } as unknown as Response;
}

const SUMMARY = {
  totalSubmittedThisWeek: 12,
  complianceRate: 0.75,
  needsCorrectionCount: 3,
  openBlockersCount: 5,
};

const TREND = [
  { weekStart: "2026-06-01", weekEnd: "2026-06-07", completedTasks: 4 },
  { weekStart: "2026-06-08", weekEnd: "2026-06-14", completedTasks: 7 },
];

const SUBMISSION_STATUS = [
  { memberName: "Alice", status: "SUBMITTED" as const },
  { memberName: "Bob", status: "NOT_STARTED" as const },
];

const WORKLOAD = [
  { projectName: "Apollo", taskCount: 9 },
  { projectName: "Zephyr", taskCount: 4 },
];

const TIME_BY_TYPE = [
  { taskType: "DEV", totalHours: 18.5 },
  { taskType: "REVIEW", totalHours: 6 },
];

const SECTION_VIEW = [
  {
    memberName: "Alice",
    status: "SUBMITTED" as const,
    items: [{ description: "Blocked on staging credentials", key: true }],
  },
];

function dashboardBackend() {
  return jest.fn(async (rawUrl: string) => {
    const { pathname } = new URL(rawUrl);
    if (pathname === "/dashboard/summary") return json(SUMMARY);
    if (pathname === "/dashboard/charts/tasks-completed-trend")
      return json(TREND);
    if (pathname === "/dashboard/charts/submission-status-by-member")
      return json(SUBMISSION_STATUS);
    if (pathname === "/dashboard/charts/workload-by-project")
      return json(WORKLOAD);
    if (pathname === "/dashboard/charts/time-by-task-type")
      return json(TIME_BY_TYPE);
    if (pathname === "/dashboard/section") return json(SECTION_VIEW);
    throw new Error(`unexpected request: ${pathname}`);
  });
}

function seedSession(role: "MANAGER" | "MEMBER") {
  window.sessionStorage.setItem("wrg.auth.token", "stored-jwt");
  window.sessionStorage.setItem(
    "wrg.auth.user",
    JSON.stringify({ id: 1, name: "Dana", role }),
  );
}

describe("ManagerDashboardPage", () => {
  beforeEach(() => {
    global.fetch = dashboardBackend() as unknown as typeof fetch;
    setAuthToken(null);
    jest.clearAllMocks();
    window.sessionStorage.clear();
  });

  it("renders the summary cards with numbers from the response", async () => {
    renderWithQueryClient(<ManagerDashboardPage />);

    expect(await screen.findByText("12")).toBeInTheDocument();
    expect(screen.getByText("Submitted this week")).toBeInTheDocument();
    expect(screen.getByText("75%")).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();
    expect(screen.getByText("5")).toBeInTheDocument();
  });

  it("renders every chart section with its data without crashing", async () => {
    renderWithQueryClient(<ManagerDashboardPage />);

    expect(screen.getByText("Tasks completed by week")).toBeInTheDocument();
    expect(screen.getByText("Workload by project")).toBeInTheDocument();
    expect(screen.getByText("Time by task type")).toBeInTheDocument();

    await waitFor(() =>
      expect(screen.getAllByTestId("bar-chart")).toHaveLength(2),
    );
    const barCharts = screen.getAllByTestId("bar-chart");
    expect(barCharts[0]).toHaveAttribute("data-count", String(TREND.length));
    expect(barCharts[1]).toHaveAttribute("data-count", String(WORKLOAD.length));
    expect(await screen.findByTestId("pie")).toHaveAttribute(
      "data-count",
      String(TIME_BY_TYPE.length),
    );

    expect(await screen.findByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("Submitted")).toBeInTheDocument();
    expect(screen.getByText("Bob")).toBeInTheDocument();
    expect(screen.getByText("Not started")).toBeInTheDocument();
  });

  it("swaps the overview for the section comparison when its tab is selected", async () => {
    renderWithQueryClient(<ManagerDashboardPage />);

    expect(await screen.findByText("Submitted this week")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("tab", { name: "Section comparison" }));

    expect(
      await screen.findByText("Blocked on staging credentials"),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Blockers" }),
    ).toBeInTheDocument();
    expect(screen.queryByText("Submitted this week")).not.toBeInTheDocument();
  });

  it("renders for a MANAGER inside the (manager) route group", async () => {
    seedSession("MANAGER");

    renderInManagerGroup(<ManagerDashboardPage />);

    expect(await screen.findByText("Team dashboard")).toBeInTheDocument();
    expect(replaceMock).not.toHaveBeenCalled();
  });

  it("redirects a non-MANAGER away from the (manager) route group", async () => {
    seedSession("MEMBER");

    renderInManagerGroup(<ManagerDashboardPage />);

    await waitFor(() => expect(replaceMock).toHaveBeenCalledWith("/reports"));
    expect(screen.queryByText("Team dashboard")).not.toBeInTheDocument();
  });
});

function renderInManagerGroup(ui: ReactNode) {
  return renderWithQueryClient(
    <AuthProvider>
      <ManagerLayout>{ui}</ManagerLayout>
    </AuthProvider>,
  );
}
