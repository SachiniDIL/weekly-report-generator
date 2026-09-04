import { render, screen, waitFor } from "@testing-library/react";
import { AuthProvider } from "@/lib/auth-context";
import DashboardPage from "./page";

const replaceMock = jest.fn();
const pushMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: pushMock, replace: replaceMock }),
}));

const AUTHENTICATED_USER = { id: 1, name: "Ada", role: "MANAGER" as const };

function renderDashboard() {
  return render(
    <AuthProvider>
      <DashboardPage />
    </AuthProvider>,
  );
}

function seedStoredSession() {
  window.sessionStorage.setItem("wrg.auth.token", "stored-jwt");
  window.sessionStorage.setItem("wrg.auth.user", JSON.stringify(AUTHENTICATED_USER));
}

function expectDashboardContentAbsent() {
  expect(screen.queryByRole("button", { name: /sign out/i })).not.toBeInTheDocument();
  expect(screen.queryByText(/welcome/i)).not.toBeInTheDocument();
}

describe("DashboardPage route guard", () => {
  beforeEach(() => {
    window.sessionStorage.clear();
    jest.clearAllMocks();
  });

  it("never renders dashboard content for a logged-out visitor and redirects to /login", async () => {
    renderDashboard();

    // Guarded content must be absent on the very first render, before the redirect even fires —
    // this is what would catch a guard that forgot to gate on isChecking.
    expectDashboardContentAbsent();

    await waitFor(() => expect(replaceMock).toHaveBeenCalledWith("/login"));

    expectDashboardContentAbsent();
  });

  it("does not redirect an authenticated user and renders their dashboard", async () => {
    seedStoredSession();

    renderDashboard();

    expect(await screen.findByText("Welcome, Ada (MANAGER)")).toBeInTheDocument();
    expect(replaceMock).not.toHaveBeenCalled();
  });
});
