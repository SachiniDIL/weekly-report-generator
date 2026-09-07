import { fireEvent, screen, waitFor } from "@testing-library/react";
import { setAuthToken } from "@/lib/api-client";
import { renderWithQueryClient } from "@/lib/test-render";
import { TeamSummaryPanel } from "./team-summary-panel";

function json(body: unknown): Response {
  return { ok: true, status: 200, json: async () => body } as unknown as Response;
}

describe("TeamSummaryPanel", () => {
  let fetchMock: jest.Mock;

  beforeEach(() => {
    fetchMock = jest.fn();
    global.fetch = fetchMock as unknown as typeof fetch;
    setAuthToken(null);
  });

  it("calls GET /ai/summary with no filter params and renders the response", async () => {
    fetchMock.mockResolvedValue(json({ message: "Completed work\n- Shipped the billing export" }));

    renderWithQueryClient(<TeamSummaryPanel />);
    fireEvent.click(screen.getByRole("button", { name: "Generate AI summary" }));

    expect(await screen.findByText(/Completed work/)).toBeInTheDocument();

    const [url, init] = fetchMock.mock.calls[0];
    const requested = new URL(url);
    expect(requested.pathname).toBe("/ai/summary");
    expect(requested.search).toBe("");
    expect(init.method).toBe("GET");
  });

  it("disables the button while the summary is generating", async () => {
    let resolveFetch: (response: Response) => void = () => {};
    fetchMock.mockImplementation(
      () =>
        new Promise<Response>((resolve) => {
          resolveFetch = resolve;
        }),
    );

    renderWithQueryClient(<TeamSummaryPanel />);
    fireEvent.click(screen.getByRole("button", { name: "Generate AI summary" }));

    await waitFor(() =>
      expect(screen.getByRole("button", { name: "Generating…" })).toBeDisabled(),
    );

    resolveFetch(json({ message: "Recurring blockers\n- None reported" }));
    expect(await screen.findByText(/Recurring blockers/)).toBeInTheDocument();
  });

  it("hides the summary when dismissed", async () => {
    fetchMock.mockResolvedValue(json({ message: "Workload imbalances\n- Evenly distributed" }));

    renderWithQueryClient(<TeamSummaryPanel />);
    fireEvent.click(screen.getByRole("button", { name: "Generate AI summary" }));

    expect(await screen.findByText(/Workload imbalances/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Dismiss summary" }));
    expect(screen.queryByText(/Workload imbalances/)).not.toBeInTheDocument();
  });
});
