import { setAuthToken } from "@/lib/api-client";
import {
  archiveProject,
  assignProjectMember,
  createProject,
  listProjects,
} from "./projects";

function response(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

describe("projects api", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
  });

  it("lists active projects by default and passes includeInactive when asked", async () => {
    fetchMock.mockResolvedValue(response(200, []));

    await listProjects();
    expect(fetchMock.mock.calls[0][0]).toBe("http://localhost:8080/projects?includeInactive=false");

    await listProjects(true);
    expect(fetchMock.mock.calls[1][0]).toBe("http://localhost:8080/projects?includeInactive=true");
  });

  it("posts the project body on create", async () => {
    fetchMock.mockResolvedValue(response(201, { id: 1, name: "Apollo", description: null, active: true }));

    await createProject({ name: "Apollo" });

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("http://localhost:8080/projects");
    expect(init.method).toBe("POST");
    expect(init.body).toBe(JSON.stringify({ name: "Apollo" }));
  });

  it("assigns a member via a path-only POST", async () => {
    fetchMock.mockResolvedValue(response(201, { userId: 7, name: "Lin", email: "lin@example.com" }));

    await assignProjectMember(3, 7);

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("http://localhost:8080/projects/3/members/7");
    expect(init.method).toBe("POST");
  });

  it("returns undefined for a 204 archive response", async () => {
    fetchMock.mockResolvedValue(response(204, null));

    await expect(archiveProject(9)).resolves.toBeUndefined();
    expect(fetchMock.mock.calls[0][1].method).toBe("DELETE");
  });
});
