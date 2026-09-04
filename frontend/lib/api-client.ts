export type Role = "ADMIN" | "MANAGER" | "MEMBER";

export interface AuthUser {
  id: number;
  name: string;
  role: Role;
}

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: AuthUser;
}

export interface ForgotPasswordPayload {
  email: string;
}

export interface ResetPasswordPayload {
  token: string;
  newPassword: string;
}

export interface MessageResponse {
  message: string;
}

/** Error thrown for any non-2xx backend response, carrying the {message, fieldErrors?} body. */
export class ApiError extends Error {
  readonly status: number;
  readonly fieldErrors?: Record<string, string>;

  constructor(status: number, message: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

// The token lives in the auth context; it is pushed here so this client stays unaware of
// how or where the session is persisted.
let authToken: string | null = null;

export function setAuthToken(token: string | null): void {
  authToken = token;
}

export function register(payload: RegisterPayload): Promise<MessageResponse> {
  return request("/auth/register", { method: "POST", body: payload });
}

export function login(payload: LoginPayload): Promise<LoginResponse> {
  return request("/auth/login", { method: "POST", body: payload });
}

export function forgotPassword(payload: ForgotPasswordPayload): Promise<MessageResponse> {
  return request("/auth/forgot-password", { method: "POST", body: payload });
}

export function resetPassword(payload: ResetPasswordPayload): Promise<MessageResponse> {
  return request("/auth/reset-password", { method: "POST", body: payload });
}

interface RequestOptions {
  method: string;
  body?: unknown;
}

async function request<T>(path: string, options: RequestOptions): Promise<T> {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }

  const response = await fetch(`${apiBaseUrl()}${path}`, {
    method: options.method,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });

  if (!response.ok) {
    throw await toApiError(response);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

async function toApiError(response: Response): Promise<ApiError> {
  const body: { message?: unknown; fieldErrors?: Record<string, string> } =
    (await response.json().catch(() => ({}))) ?? {};
  const message =
    typeof body.message === "string" ? body.message : `Request failed (${response.status})`;
  return new ApiError(response.status, message, body.fieldErrors);
}

function apiBaseUrl(): string {
  const baseUrl = process.env.NEXT_PUBLIC_API_URL;
  if (!baseUrl) {
    throw new Error("NEXT_PUBLIC_API_URL is not set");
  }
  return baseUrl;
}
