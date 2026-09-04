import { ApiError, login, register, setAuthToken } from "@/lib/api-client";

function response(status: number, body: unknown): Response {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  } as unknown as Response;
}

describe("api-client", () => {
  const fetchMock = jest.fn<Promise<Response>, [string, RequestInit]>();

  beforeEach(() => {
    global.fetch = fetchMock as unknown as typeof fetch;
    fetchMock.mockReset();
    setAuthToken(null);
  });

  it("prefixes NEXT_PUBLIC_API_URL and sets the JSON content type", async () => {
    fetchMock.mockResolvedValue(response(200, { message: "ok" }));

    await register({ name: "Ada", email: "ada@example.com", password: "password1" });

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe("http://localhost:8080/auth/register");
    expect((init.headers as Record<string, string>)["Content-Type"]).toBe("application/json");
    expect(init.body).toBe(
      JSON.stringify({ name: "Ada", email: "ada@example.com", password: "password1" }),
    );
  });

  it("attaches the bearer token once one has been set", async () => {
    setAuthToken("jwt-123");
    fetchMock.mockResolvedValue(response(200, { message: "ok" }));

    await register({ name: "Ada", email: "ada@example.com", password: "password1" });

    const init = fetchMock.mock.calls[0][1];
    expect((init.headers as Record<string, string>).Authorization).toBe("Bearer jwt-123");
  });

  it("omits the Authorization header when no token is set", async () => {
    fetchMock.mockResolvedValue(response(200, { message: "ok" }));

    await register({ name: "Ada", email: "ada@example.com", password: "password1" });

    const init = fetchMock.mock.calls[0][1];
    expect((init.headers as Record<string, string>).Authorization).toBeUndefined();
  });

  it("parses a successful login response into the typed shape", async () => {
    fetchMock.mockResolvedValue(
      response(200, { token: "jwt-abc", user: { id: 42, name: "Ada", role: "MANAGER" } }),
    );

    const result = await login({ email: "ada@example.com", password: "password1" });

    expect(result).toEqual({ token: "jwt-abc", user: { id: 42, name: "Ada", role: "MANAGER" } });
  });

  it("throws an ApiError carrying the backend message on a non-2xx response", async () => {
    fetchMock.mockResolvedValue(response(401, { message: "Invalid email or password" }));

    await expect(login({ email: "ada@example.com", password: "wrong" })).rejects.toMatchObject({
      name: "ApiError",
      status: 401,
      message: "Invalid email or password",
    });
  });

  it("exposes fieldErrors from a validation response", async () => {
    fetchMock.mockResolvedValue(
      response(400, {
        message: "Validation failed",
        fieldErrors: { password: "password must be at least 8 characters" },
      }),
    );

    const error = await register({ name: "Ada", email: "ada@example.com", password: "short" }).catch(
      (caught: unknown) => caught,
    );

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).fieldErrors).toEqual({
      password: "password must be at least 8 characters",
    });
  });
});
