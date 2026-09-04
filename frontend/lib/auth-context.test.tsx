import { act, renderHook, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";
import { ApiError, login } from "./api-client";
import { AuthProvider, useAuth } from "./auth-context";

const pushMock = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: pushMock }),
}));

jest.mock("./api-client", () => ({
  ...jest.requireActual("./api-client"),
  login: jest.fn(),
}));

const loginMock = login as jest.MockedFunction<typeof login>;

const STORED_USER = { id: 7, name: "Ada", role: "MANAGER" as const };

function wrapper({ children }: { children: ReactNode }) {
  return <AuthProvider>{children}</AuthProvider>;
}

function seedStoredSession() {
  window.sessionStorage.setItem("wrg.auth.token", "stored-jwt");
  window.sessionStorage.setItem("wrg.auth.user", JSON.stringify(STORED_USER));
}

describe("AuthProvider", () => {
  beforeEach(() => {
    window.sessionStorage.clear();
    jest.resetAllMocks();
  });

  it("rehydrates an existing session from sessionStorage on mount", async () => {
    seedStoredSession();

    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.token).toBe("stored-jwt");
    expect(result.current.user).toEqual(STORED_USER);
  });

  it("starts unauthenticated when sessionStorage is empty", async () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
  });

  it("stores the token and user in state and sessionStorage on a successful login", async () => {
    loginMock.mockResolvedValue({
      token: "fresh-jwt",
      user: { id: 3, name: "Grace", role: "MEMBER" },
    });

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(async () => {
      await result.current.login("grace@example.com", "password1");
    });

    expect(result.current.user).toEqual({ id: 3, name: "Grace", role: "MEMBER" });
    expect(result.current.token).toBe("fresh-jwt");
    expect(window.sessionStorage.getItem("wrg.auth.token")).toBe("fresh-jwt");
  });

  it("surfaces the backend error message on a failed login and stays unauthenticated", async () => {
    loginMock.mockRejectedValue(new ApiError(403, "Your account is pending admin approval"));

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    await act(async () => {
      await expect(result.current.login("pending@example.com", "password1")).rejects.toThrow(
        "Your account is pending admin approval",
      );
    });

    expect(result.current.isAuthenticated).toBe(false);
    expect(window.sessionStorage.getItem("wrg.auth.token")).toBeNull();
  });

  it("clears state and sessionStorage and redirects to /login on logout", async () => {
    seedStoredSession();

    const { result } = renderHook(() => useAuth(), { wrapper });
    await waitFor(() => expect(result.current.isAuthenticated).toBe(true));

    act(() => result.current.logout());

    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.user).toBeNull();
    expect(window.sessionStorage.getItem("wrg.auth.token")).toBeNull();
    expect(pushMock).toHaveBeenCalledWith("/login");
  });
});
