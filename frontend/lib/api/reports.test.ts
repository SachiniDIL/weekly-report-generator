import { setAuthToken } from "@/lib/api-client";
import {
  createReport,
  getReportVersionHistory,
  listReports,
  reviewReport,
  submitReport,
} from "./reports";

function response(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

describe("reports api", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
  });

  it("serializes list filters and pagination into the query string, dropping undefined keys", async () => {
    fetchMock.mockResolvedValue(response(200, { content: [] }));

    await listReports({ projectId: 4, status: "SUBMITTED", page: 1, size: 20 });

    expect(fetchMock.mock.calls[0][0]).toBe(
      "http://localhost:8080/reports?projectId=4&status=SUBMITTED&page=1&size=20",
    );
  });

  it("hits /reports with no query when no params are given", async () => {
    fetchMock.mockResolvedValue(response(200, { content: [] }));

    await listReports();

    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost:8080/reports");
  });

  it("posts the full create payload including nested content", async () => {
    fetchMock.mockResolvedValue(response(201, {}));

    const payload = {
      projectId: 2,
      weekStart: "2026-09-01",
      weekEnd: "2026-09-05",
      content: { notes: "kickoff", taskEntries: [] },
    };
    await createReport(payload);

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("http://localhost:8080/reports");
    expect(init.method).toBe("POST");
    expect(init.body).toBe(JSON.stringify(payload));
  });

  it("submits and reviews via the nested action paths", async () => {
    fetchMock.mockResolvedValue(response(200, {}));

    await submitReport(11);
    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost:8080/reports/11/submit");

    await reviewReport(11, { action: "CHANGES_REQUESTED", comment: "please revise" });
    const reviewCall = fetchMock.mock.calls[1];
    expect(reviewCall[0]).toBe("http://localhost:8080/reports/11/review");
    expect(reviewCall[1].body).toBe(
      JSON.stringify({ action: "CHANGES_REQUESTED", comment: "please revise" }),
    );
  });

  it("reads the version history from /reports/{id}/versions", async () => {
    fetchMock.mockResolvedValue(response(200, []));

    await getReportVersionHistory(11);

    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost:8080/reports/11/versions");
    expect(fetchMock.mock.calls[0][1].method).toBe("GET");
  });
});
